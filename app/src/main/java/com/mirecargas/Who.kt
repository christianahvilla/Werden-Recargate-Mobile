package com.mirecargas

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_who.*

class Who : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_who)

        support_email.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = resources.getString(R.string.activity_type_intent)
            intent.putExtra(Intent.EXTRA_EMAIL, R.string.activity_who_email)
            intent.setPackage(resources.getString(R.string.package_google))

            if (intent.resolveActivity(packageManager)!=null)
                startActivity(intent)
            else
                Toast.makeText(this@Who, R.string.gmail_error, Toast.LENGTH_LONG).show()
        }

        url_site.setOnClickListener{
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(resources.getString(R.string.activity_who_site)))
            startActivity(intent)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->{
                val intent = Intent(this@Who, Inicio::class.java)
                intent.putExtra("customer", "")
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this@Who, Inicio::class.java)
        intent.putExtra("customer", "")
        startActivity(intent)
        finish()
    }
}
