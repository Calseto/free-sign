package com.freesign.firebase.firestore.collection

import android.util.Log
import com.freesign.firebase.firestore.Firestore.firestoreInstance
import com.freesign.model.User
import com.freesign.utils.Authenticated
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlin.collections.ArrayList

object UserCollection {
    val userChannelRef = firestoreInstance.collection("users")

    var userDocumentRef = ""
    var userByUsernameDocumentRef = ""

    var user = User()
    var transactionUser = ArrayList<User>()

    fun register(user: User, onListen: (String) -> Unit) {
        userChannelRef
            .add(user)
            .addOnSuccessListener {
                userDocumentRef = it.id
                onListen(it.id)
            }
            .addOnFailureListener {
                Log.d("error", it.toString())
                onListen("error")
            }
    }

    fun updateUser(user: User, onListen: (String) -> Unit)  {
        userChannelRef
            .document(userDocumentRef)
            .update(
                "fisrtname", user.firstname,
                "lastname", user.lastname,
                "email", user.email,
                "password", user.password,
                "FCMToken", user.FCMToken
            )
            .addOnSuccessListener {
                this.user = user
                Authenticated.setUser(user)
                onListen("success")
            }
            .addOnFailureListener {
                onListen(it.message!!)
            }
    }

    fun login(email: String, password: String, role: String,onListen: (User) -> Unit): ListenerRegistration {
        return firestoreInstance.collection("users")
            .whereEqualTo("email", email)
            .whereEqualTo("password", password)
            .whereEqualTo("role", role)
            .addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "UserMessagesListener error.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                var items = User()
                querySnapshot!!.documents!!.forEach {
                    items = it.toObject(User::class.java)!!
                    if(items.firstname!=null) {
                        userByUsernameDocumentRef = it.id
                        items.id = it.id
                        Authenticated.setUser(items)
                    }
                }

                onListen(items)
            }
    }

    fun getUserByName(firstname: String, lastname: String, role: String, onListen: (User) -> Unit): ListenerRegistration {
        return userChannelRef
            .whereEqualTo("firstname", firstname)
            .whereEqualTo("lastname", lastname)
            .whereEqualTo("role", role)
            .addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "UserMessagesListener error.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                var items = User()
                querySnapshot!!.documents!!.forEach {
                    items = it.toObject(User::class.java)!!
                    userByUsernameDocumentRef = it.id
                    items.id = it.id                }

                onListen(items)
            }
    }

    fun getUserByEmail(email: String, role: String, onListen: (User) -> Unit): ListenerRegistration {
        return userChannelRef
            .whereEqualTo("email", email)
            .whereEqualTo("role", role)
            .addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "UserMessagesListener error.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                var items = User()
                querySnapshot!!.documents!!.forEach {
                    items = it.toObject(User::class.java)!!
                }

                onListen(items)
            }
    }
}