package com.github.rongi.rxpresenter.example.app.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.ViewGroup
import com.github.rongi.rxpresenter.example.R
import com.github.rongi.rxpresenter.example.app.detail.DetailActivity
import com.github.rongi.rxpresenter.example.app.main.adapter.Adapter
import com.github.rongi.rxpresenter.example.app.main.adapter.ViewHolder
import com.github.rongi.rxpresenter.example.app.main.data.Article
import com.github.rongi.rxpresenter.example.appRoots
import com.github.rongi.rxpresenter.example.common.DividerItemDecoration
import com.github.rongi.rxpresenter.example.common.clicks
import com.github.rongi.rxpresenter.example.common.onClick
import com.github.rongi.rxpresenter.example.common.visible
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.list_item.view.*
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity() {

  private lateinit var listAdapter: Adapter<Article>

  private val articleClicks = BehaviorSubject.create<Int>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_activity)
    initList()

    val model = present(
      updateButtonClicks = update_button.clicks(),
      articleClicks = articleClicks,
      articlesProvider = appRoots.articlesProvider()
    )

    render(model)
  }

  private fun render(model: MainViewModel) {
    with(model) {
      articles.render { listAdapter.items = it }
      updateButtonIsEnabled.render { update_button.isEnabled = it }
      emptyViewIsVisible.render { empty_view.visible = it }
      progressIsVisible.render { progress.visible = it }
      smallProgressIsVisible.render { progress_small.visible = it }
      updateButtonText.render { update_button.setText(it) }
      startDetailActivitySignals.render { DetailActivity.launch(this@MainActivity, it) }
    }
  }

  private fun initList() {
    recycler_view.layoutManager = LinearLayoutManager(this)
    listAdapter = createAdapter()
    recycler_view.adapter = listAdapter
    val divider = DividerItemDecoration(resources)
    recycler_view.addItemDecoration(divider)
  }

  private fun createAdapter() = Adapter(
    createView = { parent: ViewGroup, _ ->
      layoutInflater.inflate(R.layout.list_item, parent, false)
    },
    bindViewHolder = { viewHolder: ViewHolder, article: Article, position: Int ->
      viewHolder.apply {
        itemView.article_title.text = article.title
        itemView.onClick {
          articleClicks.onNext(position)
        }
      }
    }
  )

}

fun <T> Observable<T>.render(onNext: (T) -> Unit): Disposable {
  return this.observeOn(mainThread()).subscribe(onNext)
}