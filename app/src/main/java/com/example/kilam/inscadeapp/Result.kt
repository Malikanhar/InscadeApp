package com.example.kilam.inscadeapp

class Result {
    var minute : String? = null
    var second : String? = null
    var predict : String? = null

    constructor()

    constructor(minute : String?, second : String?, predict : String?){
        this.minute = minute
        this.second = second
        this.predict = predict
    }
}