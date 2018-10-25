package com.wosloveslife.tvcorsorview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import com.feiboedu.common.view.CursorView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CursorView(this).show(this)

        val fakeData = arrayListOf<String>()
        for (i in 0..100) {
            fakeData.add("测试条目$i")
        }

        rv_root.layoutManager = GridLayoutManager(this, 3, GridLayoutManager.HORIZONTAL, false)
        rv_root.adapter = RootAdapter(fakeData)
    }
}
