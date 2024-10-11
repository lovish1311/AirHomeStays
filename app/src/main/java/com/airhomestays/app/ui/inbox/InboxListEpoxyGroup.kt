package com.airhomestays.app.ui.inbox

import android.R.color
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.airhomestays.app.GetAllThreadsQuery
import com.airhomestays.app.R
import com.airhomestays.app.ViewholderInboxListMessagesBindingModel_
import com.airhomestays.app.ui.user_profile.UserProfileActivity
import java.lang.String


class InboxListEpoxyGroup (
        context : Context,
        currentPosition: Int,
        val item: GetAllThreadsQuery.Result?,
        val clickListener: (item: GetAllThreadsQuery.Result) -> Unit
) : EpoxyModelGroup(
    R.layout.model_inbox_group,
    buildModels(context, item, currentPosition, clickListener).takeIf { it.isNotEmpty() }
        ?: listOf(EmptyStateEpoxyModel()) // Provide a default model if the list is empty
) {

    init {
        id("InboxListEpoxyGroup - $currentPosition")
    }

}

fun buildModels(context: Context,item: GetAllThreadsQuery.Result?, currentPosition: Int, clickListener: (item: GetAllThreadsQuery.Result) -> Unit): List<EpoxyModel<*>> {
    val models = ArrayList<EpoxyModel<*>>()
    item?.let {
        if (item.threadItem != null){
        var isRead = item.threadItem!!.isRead
        if ((item.threadItem!!.sentBy != item.guest)
            && item.threadItem!!.isRead == false
        ) {
            isRead = false
        } else if (item.threadItem!!.sentBy!!.equals(item.guest)) {
            isRead = true
        } else {
            isRead = true
        }
        var status = true
        if (item.threadItem?.type!!.equals("message")) {
            status = false
        }
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val langType = preferences.getString("Locale.Helper.Selected.Language", "en")

        models.add(ViewholderInboxListMessagesBindingModel_()
            .id("viewholder- ${item.id}")
            .status(item.threadItem?.type!!)
            .isStatus(status)
            .avatar(item.hostProfile?.picture)
            .content(item.threadItem?.content)
            .createdAt(com.airhomestays.app.util.Utils.inboxDateFormat(item.threadItem?.createdAt!!))
            .hostName(item.hostProfile?.firstName)
            .isRead(isRead)
            .onClick { _ -> clickListener(item) }
            .avatarClick(View.OnClickListener { _ ->
                UserProfileActivity.openProfileActivity(
                    context!!,
                    it.hostProfile?.profileId!!,
                    true
                )
            })
        )
    }


    }
    return models
}
class EmptyStateEpoxyModel : EpoxyModel<View>() {
    override fun bind(view: View) {
        // No-op or set empty state message
    }
    override fun getDefaultLayout(): Int {
        return R.layout.viewholder_empty_model // Provide a layout for the empty state if needed
    }
}

