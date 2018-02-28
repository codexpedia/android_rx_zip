package com.example.rxzip

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_main.*
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.Callable
import io.reactivex.observers.DisposableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.functions.Function3
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private val disposables = CompositeDisposable()

    private fun getStrings(str1: String, str2: String): Observable<List<String>> {
        return Observable.fromCallable(object : Callable<List<String>> {
            override fun call(): List<String> {
                for (i in 0..99999999) {
                    //simulating a heavy duty computational expensive operation
                }
                log(Thread.currentThread().name + " " + str1 + " " + str2)
                val strings = ArrayList<String>()
                strings.add(str1)
                strings.add(str2)
                return strings
            }
        })
    }

    private fun mergeStringLists(): Function3<List<String>, List<String>, List<String>, List<String>> {
        return Function3 { strings, strings2, strings3 ->
            log("...")
            strings as ArrayList<String>
            strings2.forEach({
                strings.add(it)
            })

            strings3.forEach({
                strings.add(it)
            })

            strings
        }
    }

    private fun runRxZip() {
        disposables.clear()
        disposables.add(
                Observable
                        .zip(getStrings("One", "Two").subscribeOn(Schedulers.newThread()),
                                getStrings("Three", "Four").subscribeOn(Schedulers.newThread()),
                                getStrings("Five", "Six").subscribeOn(Schedulers.newThread()),
                                mergeStringLists())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<List<String>>() {
                            override fun onNext(value: List<String>) {
                                progressbar.visibility = View.GONE
                                log("combined result: " + value.toString())
                            }

                            override fun onError(e: Throwable) {
                                e.printStackTrace()
                                progressbar.visibility = View.GONE
                            }

                            override fun onComplete() {

                            }
                        })
        )
    }

    private fun log(text : String) {
        runOnUiThread {
            Log.d("rxzip", text)
            tv_display.text = "${tv_display.text.toString()}\n$text"
        }
    }

    private fun clearLog() {
        tv_display.text = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            clearLog()
            progressbar.visibility = View.VISIBLE
            runRxZip()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables?.clear()
    }

}