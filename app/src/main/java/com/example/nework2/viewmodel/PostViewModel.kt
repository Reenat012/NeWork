package com.example.nework2.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import com.example.nework2.auth.AppAuth
import com.example.nework2.dto.FeedItem
import com.example.nework2.dto.Post
import com.example.nework2.model.FeedModel
import com.example.nework2.model.FeedModelState
import com.example.nework2.model.ModelPhoto
import com.example.nework2.repository.PostRepository
import com.example.nework2.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorAvatar = "http://10.0.2.2:9999/avatars/netology.jpg",
    likedByMe = false,
    published = "",
    authorId = 0
)

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth
) :
    ViewModel() {

    private val _state = MutableLiveData<FeedModelState>() //изменяемое состояние экрана
    val state: LiveData<FeedModelState>
        get() = _state

    //переменная для подсчета скрытых новых постов
    var countHidden = 0

    //добавляем навигацию во viewModel
    private val _navigateFeedFragmentToProposalFragment = SingleLiveEvent<Unit>()
    val navigateFeedFragmentToProposalFragment: SingleLiveEvent<Unit> =
        _navigateFeedFragmentToProposalFragment


    //паттерн наблюдателя
    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<FeedItem>> =
        //впервую очередь смотрим на данные авторизации
        appAuth.data.flatMapLatest { token ->
            val myId = token?.id

            //читаем базу данных
            repository.data.map { posts ->
                posts.map { post ->
                    if (post is Post) {
                        post.copy(ownedByMe = post.authorId == myId)
                    } else post
                }
            }.flowOn(Dispatchers.Default)
        }

    private val _data = MutableLiveData<FeedModel>()

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    //подписка на количество новых постов
//    val newerCount: Flow<Int> = data.map { post ->
//        post.map { it.id
//    } }.flatMapLatest { repository.getNewer(it) }

    //сохраняем фото в переменную
    private val _photo = MutableLiveData<ModelPhoto?>(null)
    val photo: LiveData<ModelPhoto?>
        get() = _photo

    fun setPhoto(uri: Uri, file: File) {
        _photo.value = ModelPhoto(uri, file)
    }

    fun clearPhoto() {
        _photo.value = null
    }

    fun load() {
        viewModelScope.launch {
            //создаем фоновый поток
            //postValue безопасный метод в фоновом потоке вместо value, безопасно пробрасывает результат на основной поток MainThread
            _state.postValue(FeedModelState())

            _state.value = try {
                //получаем только посты сохраненные в локальной БД
                repository.getAllVisible()
                //если все хорошо, все флаги выключены
                FeedModelState()
            } catch (e: Exception) {
                //если ошибка, выбрасываем предупреждение
                FeedModelState(error = true)
            }
        }
    }

    fun getHiddenCount() {
        viewModelScope.launch {
            //обновляем переменную count числом новых постов
            countHidden = repository.getHiddenCount().firstOrNull() ?: 0
        }
    }

    suspend fun changeHiddenPosts() {
        repository.changeHiddenPosts()
    }

    fun refreshPosts() {
        //создаем корутину
        viewModelScope.launch {
            //создаем фоновый поток
            //вызываем значок загрузки
            //postValue безопасный метод в фоновом потоке вместо value, безопасно пробрасывает результат на основной поток MainThread
            _state.postValue(FeedModelState(refreshing = true))

            _state.value = try {
                repository.getAll()
                //если все хорошо, все флаги выключены
                FeedModelState()
            } catch (e: Exception) {
                //если ошибка, выбрасываем предупреждение
                FeedModelState(error = true)
            }
        }
    }

    init {
        load()
    }

    //запрос списка постов

    val edited = MutableLiveData(empty) //хранит состояние редактированного поста

    fun changeContentAndSave(text: String) {
        viewModelScope.launch {
            edited.value?.let { post ->
                try {
                    //если текст не равен существующему
                    //добавлено фото
                    if (post.content != text.trim() && _photo.value != null) {
                        _photo.value?.let {
                            repository.saveWithAttachment(post.copy(content = text), it)
                        }
                    }
                    //если текст равен существующему(т.е. его не надо менять)
                    //добавлено фото
                    else if (post.content == text.trim() && _photo.value != null) {
                        _photo.value?.let {
                            repository.saveWithAttachment(post, it)
                        }
                    }
                    //если текст не равен существующему
                    //фото не добавлено
                    else if (post.content != text.trim() && _photo.value == null) {
                        repository.saveAsync(post.copy(content = text))
                    }

                    //postValue обновляем с фонового потока LiveData}
                    edited.postValue(empty)
                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                }
            }

        }
    }

    //функция отмены редактирования и очистка поста
    fun cancelEdit() {
        edited.value = empty
    }

    fun cancelChangeContent() {
        edited.value = empty //удаляем редактированный пост из поля для редактирования
    }

    fun repost(id: Long) = repository.repost(id)
//    fun likeById(id: Long) {
//        thread {
//            repository.likeById(id)
//            repository.getPost(id) //обращаемся к репозиторию и обновляем пост
//        }
//    }

    fun likeById(id: Long) {
        //нужно создать условие
        //если пользователь вошел (если есть токен), можно лайкнуть
        //если нет, предложить авторизоваться

        if (appAuth.data.value != null) {
            //получаем состояние likedByMe поста по id
            val likedByMe = _data.value?.posts?.find { it.id == id }?.likedByMe ?: return

            viewModelScope.launch {
                //если true - dislike, false - like
                try {
                    if (likedByMe) {
                        repository.disLikeByIdAsync(id)
                        //обновлять данные вручную не требуется так как они обновляются автоматически благодаря подписке к локальной базе данных
                    } else {
                        repository.likeByIdAsync(id)
                    }
                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                }
            }
        } else {
            //инициируем переход
            _navigateFeedFragmentToProposalFragment.postValue(Unit)
        }
    }

    fun removeById(id: Long) {
        // Оптимистичная модель
        viewModelScope.launch {
            val old = _data.value?.posts.orEmpty()

            try {
                repository.removeByIdAsync(id)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun edit(post: Post) {
        edited.value = post //редактируемый пост записываем в LiveData edited
    }

    fun playVideo(post: Post) {
        post.video
    }

    fun openPost(post: Post) {
//        repository.openPostById(post.id)
    }
}