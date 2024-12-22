package com.example.nework2.util

import android.os.Bundle
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

//необходим для передачи данных между фрагментами
object StringArg: ReadWriteProperty<Bundle, String?> {

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: String?) {
        thisRef.putString(property.name, value)
    }

    override fun getValue(thisRef: Bundle, property: KProperty<*>): String? =
        thisRef.getString(property.name)
}