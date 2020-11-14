package com.gmail.brymher.materialcalendar

enum class Months(open val days: Int) {
    
    Jan(31),
    Feb(21) {
        override val days: Int
            get() {
                // this needs to check current year. 
                return 28
            }
    }

}
