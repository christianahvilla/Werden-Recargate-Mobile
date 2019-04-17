package com.mirecargas

import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast

import kotlinx.android.synthetic.main.content_inicio.*
import com.mirecargas.Models.Customer
import com.mirecargas.Sqlite.SetDB
import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.support.design.widget.NavigationView
import android.support.v4.view.MenuItemCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.SearchView
import kotlinx.android.synthetic.main.app_bar_inicio.*
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v4.view.GravityCompat
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.google.zxing.Result
import com.mirecargas.Adapters.AdapterCustomers
import com.mirecargas.Models.User
import com.mirecargas.Sqlite.CustomerDB
import com.mirecargas.Sqlite.UserDB
import kotlinx.android.synthetic.main.activity_inicio.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.json.JSONObject


class Inicio : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ZXingScannerView.ResultHandler {

    //QR Scan
    private val permissionRequestCode = 200
    private lateinit var zXingScannerView: ZXingScannerView
    private var isRecording = false

    //Variables for list
    private var customers: ArrayList<Customer> = ArrayList()
    private var adapter: AdapterCustomers? = null

    //Variables for database
    private var customerDB: CustomerDB = CustomerDB()
    private lateinit var db: SetDB
    private var userList: ArrayList<User> = ArrayList()

    //Values for animations
    private var isOpen = false
    private var isSearching = false
    private lateinit var fabOpen:Animation
    private lateinit var fabClose: Animation
    private lateinit var fabRClockwise: Animation
    private lateinit var fabRanticlockwise: Animation
    private lateinit var fabHide: Animation
    private lateinit var fabShow: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)
        setSupportActionBar(toolbar)

        db = SetDB(this)

        fabOpen = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close)
        fabRClockwise = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_clokwise)
        fabRanticlockwise = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_anticlockwise)
        fabShow = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_show_animation)
        fabHide = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_hide_animation)

        getCustomers(customerDB.getCustomer(db))
        getUsers()
        createRecycler()
        runLayoutAnimation()
        setRecyclerViewScrollListener()

        fab_menu.setOnClickListener{
           startAnimation()
        }

        fab_add.setOnClickListener {
            val dialog = dialogAddCustomer()
            startAnimation()
            dialog.show()
        }

        fab_qr.setOnClickListener{
            startAnimation()
            validatePermissions()
        }

        showDialogCopyCustomer()

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_close, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        navigationView.menu.getItem(0).isCheckable = false
        navigationView.menu.getItem(1).isCheckable = false
    }

    override fun handleResult(rawResult: Result) {
        zXingScannerView.stopCamera()

        val intent = Intent(this@Inicio, Inicio::class.java)
        intent.putExtra("customer", rawResult.text)
        startActivity(intent)
        finish()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        val intent: Intent

        when (id) {

            R.id.nav_map -> {
                intent = Intent(Intent.ACTION_VIEW, Uri.parse(resources.getString(R.string.activity_who_site)))
                startActivity(intent)
            }

            R.id.nav_who -> {
                intent = Intent(this@Inicio, Who::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_change_profile ->{
                val dialog = this.dialogChangeProfile()
                dialog.show()
            }
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    @Suppress("DEPRECATION")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.search, menu)
        // Retrieve the SearchView and plug it into SearchManager
        val searchView = MenuItemCompat.getActionView(menu!!.findItem(R.id.action_search)) as SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        stylingSearchView(searchView)


        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            isSearching = hasFocus
            if(hasFocus) {
                stylingSearchViewOnFocus(searchView)
                startSearching()
            }
            else {
                stylingSearchView(searchView)
                endScrollAnimation()
            }
        }

        searchView.setOnQueryTextListener( object  : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                adapter!!.filter.filter(newText)
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                adapter!!.filter.filter(query)
                return false
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        when {
            isRecording -> {
                val intent = Intent(this@Inicio, Inicio::class.java)
                intent.putExtra("customer", "")
                startActivity(intent)
                isRecording = false
                finish()
            }
            drawer_layout.isDrawerOpen(GravityCompat.START) -> drawer_layout.closeDrawer(GravityCompat.START)
            else -> finish()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isRecording)
            zXingScannerView.stopCamera()

    }

    override fun onPostResume() {
        super.onPostResume()
        if(isRecording)
            zXingScannerView.startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(isRecording)
            zXingScannerView.stopCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode){
             permissionRequestCode -> {
                if (ContextCompat.checkSelfPermission(this, CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                    startScan()
                }
                else{
                    val dialog = dialogAlertCamera()
                    dialog.show()
                }
            }
        }

    }

    private fun getUsers() {
        val userDB = UserDB()
        userList = userDB.getUser(db)
    }

    private fun stylingSearchView(searchView: SearchView){
        searchView.setIconifiedByDefault(false)

        val searchPlate = searchView.findViewById<SearchView.SearchAutoComplete>(android.support.v7.appcompat.R.id.search_src_text)
        searchPlate.setTextColor(Color.WHITE)
        searchPlate.setHintTextColor(Color.WHITE)
        searchPlate.hint = resources.getString(R.string.app_name)

        /*val searchViewIcon = searchView.findViewById<ImageView>(android.support.v7.appcompat.R.id.search_mag_icon)
        val linearLayoutSearchView = searchViewIcon.parent as ViewGroup
        linearLayoutSearchView.removeView(searchViewIcon)*/
    }

    private fun stylingSearchViewOnFocus(searchView: SearchView){
        searchView.setIconifiedByDefault(false)

        val searchPlate = searchView.findViewById<SearchView.SearchAutoComplete>(android.support.v7.appcompat.R.id.search_src_text)
        searchPlate.setHintTextColor(resources.getColor(R.color.searchView_hint))
        searchPlate.hint = resources.getString(R.string.app_name)

        /*val searchViewIcon = searchView.findViewById<ImageView>(android.support.v7.appcompat.R.id.search_mag_icon)
        val linearLayoutSearchView = searchViewIcon.parent as ViewGroup
        linearLayoutSearchView.removeView(searchViewIcon)*/
    }

    private fun startScan(){
        zXingScannerView = ZXingScannerView(this@Inicio)
        setContentView(zXingScannerView)
        zXingScannerView.setResultHandler(this@Inicio)
        zXingScannerView.setAutoFocus(true)
        zXingScannerView.startCamera()
        isRecording = true
    }

    private fun showDialogCopyCustomer() {
        if ( !intent.getStringExtra("customer").isEmpty() ){
            val customerString = intent.getStringExtra("customer")
            val mainObject = JSONObject(customerString)
            val customer = Customer()

            customer.name = mainObject.getString("name")
            customer.last = mainObject.getString("last")
            customer.number = mainObject.getString("number")
            customer.carrier = mainObject.getString("carrier")

            avoidDuplicated(customer)
            copyToClip(customer.number)

            val dialog = dialogCustomerAdded(customer)
            if(userList[0].root == "1") dialog.show()
        }
    }

    private fun validatePermissions(){
        if (ContextCompat.checkSelfPermission(this, CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
                startScan()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA)) {
                val dialog = dialogAlertCamera()
                dialog.show()
            } else {
                ActivityCompat.requestPermissions(
                    this@Inicio, arrayOf(android.Manifest.permission.CAMERA), permissionRequestCode)
            }
        }
    }

    private fun dialogAlertCamera(): AlertDialog.Builder{
        val dialog = AlertDialog.Builder(this)

        dialog.setTitle(resources.getString(R.string.alert_title_camera))
        dialog.setMessage(resources.getString(R.string.alert_message_camera))

        dialog.setPositiveButton(R.string.button_accept) { _, _ ->
        }

        return dialog
    }

    private fun avoidDuplicated(customer: Customer){
        if(!customerDB.findNumber(customer.number,db)){
            customerDB.saveCustomer(customer,db)
            getCustomers(customerDB.getCustomer(db))
            createRecycler()
            runLayoutAnimation()

            if(userList[0].root == "1"){
                val dialog = dialogCustomerAdded(customer)
                dialog.show()
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun dialogChangeProfile(): AlertDialog.Builder{
        val dialog = AlertDialog.Builder(this)
        val inflater: LayoutInflater = layoutInflater
        val view = inflater.inflate(R.layout.change_profile, null)
        dialog.setView(view)
        val code = view.findViewById<TextInputEditText>(R.id.change_code)

        dialog.setNeutralButton(R.string.button_cancel) { _, _ -> }

        dialog.setPositiveButton(R.string.button_accept) { _, _ ->
            if(code.text.toString() == resources.getString(R.string.codeChange)){
                changeProfile()
            }
            else{
                Toast.makeText(this,R.string.toast_code,Toast.LENGTH_SHORT).show()
            }
        }

        return dialog
    }

    @SuppressLint("InflateParams")
    private fun dialogCustomerAdded(customer: Customer): AlertDialog.Builder{
        val dialog = AlertDialog.Builder(this)
        val inflater: LayoutInflater = layoutInflater
        val view = inflater.inflate(R.layout.copy_customer, null)

        dialog.setTitle(R.string.alert_title_numero_copiado)
        dialog.setView(view)

        val name = view.findViewById<TextView>(R.id.copy_name)
        val last = view.findViewById<TextView>(R.id.copy_last)
        val number = view.findViewById<TextView>(R.id.copy_number)
        val carrier = view.findViewById<TextView>(R.id.copy_carrier)

        name.text = customer.name
        last.text = customer.last
        number.text = customer.number
        carrier.text = customer.carrier

        dialog.setPositiveButton(R.string.button_accept) { _, _ ->
        }

        return dialog
    }

    private fun changeProfile() {
        val userDB = UserDB()
        val user = User()
        val intent = Intent(this@Inicio, SplashScreen::class.java)
        val message = if (userList[0].root == "0") resources.getString(R.string.activity_inicio_change_profile) else resources.getString(R.string.activity_inicio_change_noroot)

        user.id = "1"
        user.root = if (userList[0].root == "0") "1" else "0"
        userDB.updateUser(user, db)

        Toast.makeText(this@Inicio, message, Toast.LENGTH_SHORT).show()
        startActivity(intent)
        finish()
    }

    private fun copyToClip(number: CharSequence){
        val clip: ClipData = ClipData.newPlainText("number", number)
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip = clip
    }

    private fun startAnimation(){
        if (isOpen) {
            fab_add.startAnimation(fabClose)
            fab_qr.startAnimation(fabClose)
            fab_menu.startAnimation(fabRanticlockwise)
            fab_qr.isClickable=false
            fab_add.isClickable = false
            isOpen = false
        } else {
            fab_add.startAnimation(fabOpen)
            fab_qr.startAnimation(fabOpen)
            fab_menu.startAnimation(fabRClockwise)
            fab_qr.isClickable = true
            fab_add.isClickable = true
            isOpen = true
        }
    }

    @SuppressLint("InflateParams")
    private fun dialogAddCustomer(): AlertDialog.Builder{
        val dialog = AlertDialog.Builder(this)
        val inflater: LayoutInflater = layoutInflater
        val view = inflater.inflate(R.layout.add_customer, null)
        dialog.setView(view)
        val customer = Customer()
        val name = view.findViewById<TextInputEditText>(R.id.name)
        val last = view.findViewById<TextInputEditText>(R.id.last)
        val number = view.findViewById<TextInputEditText>(R.id.number)
        val carrier = view.findViewById<TextInputEditText>(R.id.carrier)

        dialog.setNeutralButton(R.string.button_cancel) { _, _ -> }

        dialog.setPositiveButton(R.string.button_accept) { _, _ ->
            customer.name = name.text.toString()
            customer.last = last.text.toString()
            customer.number = number.text.toString()
            customer.carrier = carrier.text.toString()
            if (validation(customer)) {
                avoidDuplicated(customer)
            }
        }
        return dialog
    }

    private fun validation(customer: Customer): Boolean{
        val regex ="[0-9]{10}".toRegex()
        if (customer.name.length >= 3) {
            if (customer.last.length >= 3) {
                return if (customer.number.length > 9 && regex.matches(customer.number)) {
                    if( customer.carrier.length >= 4){
                        true
                    } else{
                        Toast.makeText(this,R.string.toast_carrier,Toast.LENGTH_SHORT).show()
                        false
                    }
                } else{
                    Toast.makeText(this,R.string.toast_number,Toast.LENGTH_SHORT).show()
                    false
                }
            }
            else{
                Toast.makeText(this,R.string.toast_last,Toast.LENGTH_SHORT).show()
                return false
            }
        }
        else{
            Toast.makeText(this,R.string.toast_name,Toast.LENGTH_SHORT).show()
            return false
        }
    }

    private fun getCustomers(customers: ArrayList<Customer> ) {
        this.customers = customers
    }

    private fun createRecycler(){
        val lim = LinearLayoutManager(this)
        lim.orientation = LinearLayoutManager.VERTICAL
        recyclerCustomers.layoutManager = lim
        adapter = AdapterCustomers(customers,userList, this@Inicio, db)
        recyclerCustomers.adapter = adapter
    }

    private fun runLayoutAnimation(){
        val controller= AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down)
        recyclerCustomers.layoutAnimation = controller
        recyclerCustomers.adapter!!.notifyDataSetChanged()
        recyclerCustomers.scheduleLayoutAnimation()
    }

    private fun setRecyclerViewScrollListener(){
        val scrollListener = object :RecyclerView.OnScrollListener(){

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when(newState) {

                    RecyclerView.SCROLL_STATE_IDLE -> {
                        endScrollAnimation()
                    }

                    RecyclerView.SCREEN_STATE_ON -> {
                        startScrollAnimation()
                    }
                }

            }
        }

        recyclerCustomers.addOnScrollListener(scrollListener)
    }

    @SuppressLint("RestrictedApi")
    private fun startScrollAnimation(){
        //val imm = this@Inicio.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if(!isSearching){
            if (isOpen) {
                fab_add.startAnimation(fabHide)
                fab_qr.startAnimation(fabHide)
                fab_qr.isClickable=false
                fab_add.isClickable = false
                isOpen = false
            }
            fab_menu.startAnimation(fabHide)
            fab_menu.visibility = View.INVISIBLE
            fab_menu.isClickable = false
        }
        //println("start " + imm.isAcceptingText)
    }

    @SuppressLint("RestrictedApi")
    private fun startSearching(){
        //val imm = this@Inicio.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if(isSearching){
            if (isOpen) {
                fab_add.startAnimation(fabHide)
                fab_qr.startAnimation(fabHide)
                fab_qr.isClickable=false
                fab_add.isClickable = false
                isOpen = false
            }
            fab_menu.startAnimation(fabHide)
            fab_menu.visibility = View.INVISIBLE
            fab_menu.isClickable = false
        }
        //println("start " + imm.isAcceptingText)
    }

    @SuppressLint("RestrictedApi")
    private fun endScrollAnimation(){
        //val imm = this@Inicio.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        //println("end " + imm.isAcceptingText)
        if(!isSearching){
            fab_menu.visibility = View.VISIBLE
            fab_menu.isClickable = true
            fab_menu.startAnimation(fabShow)
        }
    }

}
