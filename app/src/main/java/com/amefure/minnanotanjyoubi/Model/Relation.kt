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

    companion object {
        fun getIndex(value: String): Int {
            when(value) {
                FRIEND.value() -> return 0
                FAMILY.value() -> return 1
                SCHOOL.value() -> return 2
                WORK.value() -> return 3
                OTHER.value() -> return 4
                else -> return 0
            }
        }

        fun getRelation(value: String): Relation {
            when(value) {
                "0"-> return FRIEND
                "1" -> return FAMILY
                "2" -> return SCHOOL
                "3" -> return WORK
                "4" -> return OTHER
                else -> return FRIEND
            }
        }
    }
}