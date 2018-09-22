package com.me.hatem.a02_kt_fa.Model

class Channel(val name: String, val description: String, val id: String) {
    override fun toString(): String {
        return "#$name"
    }
}