package com.hamzawy.sat.data
object AuthManager{
fun isRemoved(id:String,all:List<Technician>):Boolean=all.none{it.id==id}
fun canLogin(tech:Technician?):Boolean=tech!=null
}
