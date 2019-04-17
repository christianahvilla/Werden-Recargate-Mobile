package com.mirecargas

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import com.mirecargas.Models.User
import com.mirecargas.Sqlite.SetDB
import com.mirecargas.Sqlite.UserDB
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreen : AppCompatActivity() {

    private val mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val setDB = SetDB(this@SplashScreen)
        addUser(setDB)

        val monitor = Runnable {
            mHandler.postDelayed(openInicio(this),2500)
        }

        val ani = AnimationUtils.loadAnimation(this, R.anim.splashanimation)
        logo.startAnimation(ani)
        monitor.run()
    }

    private fun openInicio(context: Context): Runnable {
        return Runnable{
            val intent = Intent(context, Inicio::class.java)
            intent.putExtra("customer", "")
            context.startActivity(intent)
            finish()
        }
    }

    private fun addUser(setDB: SetDB){
        val userDB = UserDB()

        val listUser = userDB.getUser(setDB)

        if (listUser.isEmpty()){
            val user = User()
            user.root = "0"
            userDB.saveUser(user, setDB)
        }

    }

}
