package com.example.kilam.inscadeapp

class Patient{
//    var patientkey : String? = null
    var name : String = ""
    var age : String = ""
    var gender : String = ""

    constructor()

    constructor(name : String, age : String, gender : String){
//        this.patientkey = patientKey
        this.name = name
        this.age = age
        this.gender = gender
    }
}