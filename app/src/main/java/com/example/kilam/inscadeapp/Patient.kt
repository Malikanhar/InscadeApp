package com.example.kilam.inscadeapp

class Patient{
    var patientkey : String? = null
    var name : String? = null
    var age : String? = null
    var gender : String? = null

    constructor()

    constructor(patientKey : String?, name : String?, age : String?, gender : String?){
        this.patientkey = patientKey
        this.name = name
        this.age = age
        this.gender = gender
    }
}