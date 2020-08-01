package com.yinghaowu.saucenaosearch

fun main() {
    val dbList = ArrayList<Int>()
    (2..12).forEach {
        dbList.add(it)

    }
    dbList.remove(4)
    println(dbList)

}