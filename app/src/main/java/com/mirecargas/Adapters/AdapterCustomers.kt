package com.mirecargas.Adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.mirecargas.Models.Customer
import com.mirecargas.Models.User
import com.mirecargas.QR
import com.mirecargas.R
import com.mirecargas.Sqlite.CustomerDB
import com.mirecargas.Sqlite.SetDB
import java.util.ArrayList
import android.app.Activity
import org.json.JSONObject
import java.text.Normalizer


class AdapterCustomers(private var customers: ArrayList<Customer>, private val users: ArrayList<User>, private val context: Context, private var setDB: SetDB): RecyclerView.Adapter<ViewHolder>(), Filterable {

    private var mFilteredList = customers
    private var customerDB: CustomerDB = CustomerDB()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.customer_row,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return customers.size
    }

    override fun getFilter(): Filter {

        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {

                val charString = unAccent(charSequence)

                if (charString.isEmpty()) {
                    customers = mFilteredList
                }
                else {

                   customers = mFilteredList

                    val filteredList = ArrayList<Customer>()

                    for (c in customers) {

                        val charStringCustomer = unAccent(c.name + c.last + c.number)

                        if (charStringCustomer.contains(charString)) {
                            filteredList.add(c)
                        }
                    }

                    customers = filteredList
                }

                val filterResults = FilterResults()
                filterResults.values = customers
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                customers = filterResults.values as ArrayList<Customer>
                notifyDataSetChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = customers[position].name
        holder.last.text = customers[position].last
        holder.carrier.text = customers[position].carrier
        holder.number.text = customers[position].number

        holder.menu.setOnClickListener {
            val popup = PopupMenu(context, holder.menu)
            popup.inflate(R.menu.customer_menu)

            popup.setOnMenuItemClickListener { menuItem ->
                when(menuItem.itemId){
                    R.id.del_customer -> {
                        val message = context.resources.getString(R.string.toast_client_delete) +" "+ customers[position].name + " " + customers[position].last+"?"
                        builderDel(customers[position], position, message,1).show()
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
            popup.show()
        }

        holder.layout.setOnClickListener{
            decideOnUser(customers[position])
        }

    }

    private fun decideOnUser(customer: Customer){
        if ( users[0].root == "0") {

            val intent = Intent(context,QR::class.java)

            intent.putExtra("customer", createJSONObject(customer).toString())
            intent.putExtra("number", customer.number)

            context.startActivity(intent)
            (context as Activity).finish()
        }
        else{
            copyToClip(customer.number)
            val dialog = dialogCustomerAdded(customer)
            dialog.show()
        }
    }

    private fun createJSONObject(customer: Customer): JSONObject{
        val jsonObject = JSONObject()

        jsonObject.put("id", customer.id)
        jsonObject.put("name", customer.name)
        jsonObject.put("last", customer.last)
        jsonObject.put("number", customer.number)
        jsonObject.put("carrier", customer.carrier)

        return jsonObject
    }

    private fun builderDel(customer: Customer, position: Int, message: String, op: Int): AlertDialog.Builder{
        val dialog = AlertDialog.Builder(context)

        dialog.setTitle(R.string.alert_title_hola)
        dialog.setMessage(message)

        dialog.setNeutralButton(R.string.button_cancel) { _, _ -> }

        when(op){
            1 -> {
                dialog.setPositiveButton(R.string.button_accept) { _, _ ->
                    customerDB.delCustomer(customer, setDB)
                    customers.removeAt(position)
                    notifyDataSetChanged()
                }
            }

            2 -> {
                dialog.setPositiveButton(R.string.button_accept) { _, _ ->
                    copyToClip(customer.number)
                    Toast.makeText(context,customer.number+" copiado",Toast.LENGTH_SHORT).show()
                }
            }
        }

        return dialog
    }

    private fun dialogCustomerAdded(customer: Customer): AlertDialog.Builder{
        val dialog = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
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

    private fun unAccent(charSequence: CharSequence): String{

        val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        val string = charSequence.toString().replace(" ","").toLowerCase()

        val temp = Normalizer.normalize(string, Normalizer.Form.NFD)
        return REGEX_UNACCENT.replace(temp, "")

    }

    private fun copyToClip(number: CharSequence){
        val clip: ClipData = ClipData.newPlainText("number", number)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip = clip
    }

}

class ViewHolder (view: View): RecyclerView.ViewHolder(view){
    val name: TextView = view.findViewById(R.id.name_list)
    val last: TextView = view.findViewById(R.id.last_list)
    val carrier: TextView = view.findViewById(R.id.carrier_list)
    val number: TextView = view.findViewById(R.id.number_list)
    val menu: TextView = view.findViewById(R.id.menuButton)
    val layout: LinearLayout = view.findViewById(R.id.layout_customer)
}