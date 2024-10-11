package com.airhomestays.app.ui.host.hostInbox

import android.view.View
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.airhomestays.app.GetAllThreadsQuery
import com.airhomestays.app.R
import com.airhomestays.app.ViewholderInboxListMessagesBindingModel_

class HostInboxListEpoxyGroup(
        currentPosition: Int,
        val item: GetAllThreadsQuery.Result,
        val clickListener: (item: GetAllThreadsQuery.Result) -> Unit,
        val avatarClick: (item: GetAllThreadsQuery.Result) -> Unit
) : EpoxyModelGroup(R.layout.model_inbox_group, buildModels(item, currentPosition, clickListener, avatarClick)) {

    init {
        id("InboxListEpoxyGroup - $currentPosition")
    }

}

fun buildModels(item: GetAllThreadsQuery.Result?, currentPosition: Int, clickListener: (item: GetAllThreadsQuery.Result) -> Unit,
                avatarClick: (item: GetAllThreadsQuery.Result) -> Unit): List<EpoxyModel<*>> {
    val models = ArrayList<EpoxyModel<*>>()
    item?.let {
        var isRead = item.threadItem?.isRead
        if ((item.threadItem?.sentBy == item.guest)
                && item.threadItem?.isRead == false) {
            isRead = false
        }else if((item.threadItem?.sentBy?:"").equals(item.host)){
            isRead = true
        }else{
            isRead = true
        }
        var status = true
        if((item.threadItem?.type?:"").equals("message")){
            status = false
        }

        models.add(ViewholderInboxListMessagesBindingModel_()
                .id("viewholder- ${item.id}")
                .status(item.threadItem?.type?:"")
                .isStatus(status)
                .avatar(item.guestProfile?.picture)
                .content(item.threadItem?.content)
                .createdAt(com.airhomestays.app.util.Utils.inboxDateFormat(item.threadItem?.createdAt?:""))
                .hostName(item.guestProfile?.firstName)
                .isRead(isRead)
                .onClick { _ -> clickListener(item) }
                .avatarClick(View.OnClickListener { _ ->
                    avatarClick(item)
                }))

    }
    return models
}

