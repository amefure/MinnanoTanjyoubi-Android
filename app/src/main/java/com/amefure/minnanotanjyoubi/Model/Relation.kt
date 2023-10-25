package com.amefure.minnanotanjyoubi.Model

enum class Relation {
    FRIEND {
        override fun value():String = "友達"
    },
    FAMILY {
        override fun value():String = "家族"
    },
    SCHOOL {
        override fun value():String = "学校"
    },
    WORK {
        override fun value():String = "仕事"
    },
    OTHER {
        override fun value():String = "その他"
    };
    abstract fun value(): String
}