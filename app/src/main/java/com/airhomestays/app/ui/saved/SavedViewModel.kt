package com.airhomestays.app.ui.saved

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.PagedList
import com.apollographql.apollo3.api.ApolloResponse
import com.airhomestays.app.CreateWishListMutation
import com.airhomestays.app.DeleteWishListGroupMutation
import com.airhomestays.app.GetAllWishListGroupQuery
import com.airhomestays.app.GetAllWishListGroupWithoutPageQuery
import com.airhomestays.app.GetWishListGroupQuery
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.data.remote.paging.Listing
import com.airhomestays.app.ui.auth.AuthViewModel
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import com.airhomestays.app.vo.OneTotalPrice
import com.airhomestays.app.vo.Photo
import com.airhomestays.app.vo.SavedList
import com.airhomestays.app.vo.SearchListing
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SavedViewModel @Inject constructor(
    dataManager: DataManager,
    private val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
): BaseViewModel<SavedNavigator>(dataManager,resourceProvider) {

    var createdWishlistGroup = ArrayList<SavedList>()
    lateinit var wishListGroup : MutableLiveData<List<SavedList>?>
    val wishListGroupCopy = MutableLiveData<List<SavedList>?>()
    private  var wishList  =  MutableLiveData<List<GetWishListGroupQuery.WishList?>>()
    var wishlistType : Int? = 0
    var listId = MutableLiveData<Int>()
    var listImage = MutableLiveData<String>()
    var listGroupCount = MutableLiveData<Int>()
    var screen = MutableLiveData<String>()
    var isSimilar = MutableLiveData<Boolean>()

    var isLoadingInProgess = MutableLiveData<Int>().apply { value = 0 }

    var retryArray = MutableLiveData<ArrayList<String>>()

    var isRefreshing = false

    val isWishListAdded = MutableLiveData<Boolean>()

    val firstSetValue = MutableLiveData<Boolean>().apply { value = false }

    var retryCalled = ""
    var duplicateid=0

    fun getIsWishListAdded(): Boolean {
        return isWishListAdded.value?.let { it } ?: false
    }

    fun setListDetails(id: Int, image: String, count: Int) {
        listId.value = id
        listImage.value = image
        listGroupCount.value = count
    }

    fun loadWishListGroup(): MutableLiveData<List<SavedList>?> {
        if (!::wishListGroup.isInitialized) {
            wishListGroup = MutableLiveData()
            getAllWishListGroup()

        }
        return wishListGroup
    }

    fun getAllWishListGroup() {
        val buildMutation = GetAllWishListGroupQuery()

        compositeDisposable.add(dataManager.getAllWishListGroup(buildMutation)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    if (response.data?.getAllWishListGroup?.status == 200) {
                        wishListGroup.value = parseList(response.data?.getAllWishListGroup?.results)
                        wishListGroupCopy.value = parseList(response.data?.getAllWishListGroup?.results)//wishListGroup.value
                    } else {
                        wishListGroup.value = emptyList()
                    }
                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, {
                handleException(it, true)
            } )
        )
    }

    fun getWishList(page: Int) {
        val buildMutation = GetWishListGroupQuery(currentPage = 1.toOptional(), id = 0)
        compositeDisposable.add(dataManager.getWishListGroup(buildMutation)
            .delay(1, TimeUnit.SECONDS)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    if (response.data?.getWishListGroup?.status == 200) {
                        wishList.value = response.data?.getWishListGroup!!.results!!.wishLists!!
                    } else {
                        if (response.data?.getWishListGroup?.errorMessage==null){
                            navigator.showError()
                        }else{
                            navigator.showToast(response.data?.getWishListGroup?.errorMessage.toString())
                        }

                    }
                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, { handleException(it) } )
        )
    }

    private fun parseList(results: List<GetAllWishListGroupQuery.Result?>?): List<SavedList>? {
        val list = ArrayList<SavedList>()
        try {
            results?.forEachIndexed { index, result ->
                var isInGroup = false
                result?.wishListIds?.let {
                    if (it.contains(listId.value)) {
                        isInGroup = true
                    }
                }

                list.add(SavedList(
                    result?.id!!,
                    result?.name!!,
                    result?.wishListCover?.listData?.listPhotoName,
                    result?.wishListCount,
                    isInGroup,
                    result.id.toString()
                ))
            }
            list.reverse()
        } catch (e: Exception) {
            navigator.showError()
        }
        return list
    }

    fun createWishList(listId: Int, groupId: Int, eventKey: Boolean, flag: Boolean = false) {
        isWishListAdded.value=false
        isLoadingInProgess.value = isLoadingInProgess.value?.plus(1)
        val buildMutation = CreateWishListMutation(
            listId = listId,
            wishListGroupId = groupId.toOptional(),
            eventKey = eventKey.toOptional()
        )

        compositeDisposable.add(dataManager.CreateWishList(buildMutation)
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    if (response.data?.createWishList?.status == 200) {
                        retryCalled = ""
                        if (flag) {
                            navigator.reloadExplore()
                            deleteList(listId)
                        } else {
                            isWishListAdded.value = true
                            changeWishListStatus(groupId, eventKey)
                            //getAllWishListGroup()
                        }
                        getWholeWishListGroup()
                    } else {
                        isLoadingInProgess.value = isLoadingInProgess.value?.minus(1)
                        navigator.showToast(response.data?.createWishList?.errorMessage.toString())
                        navigator.moveUpScreen()
                    }
                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                    isLoadingInProgess.value = isLoadingInProgess.value?.minus(1)
                    navigator.showError()
                }
            }, {
                isLoadingInProgess.value = isLoadingInProgess.value?.minus(1)
                if(!flag) {
                    changeRetry(groupId)
                }
                handleException(it, true)
            } )
        )
    }

    fun changeRetry(groupID: Int){
        val list = wishListGroup.value
        list?.forEachIndexed { index, savedList: SavedList ->
            if(savedList.id == groupID) {
                savedList.progress = AuthViewModel.LottieProgress.NORMAL
                savedList.isRetry = retryCalled
            }
        }
        wishListGroup.value = list
        isLoadingInProgess.value = isLoadingInProgess.value?.minus(1)
    }

    private fun deleteList(listId: Int) {
        try {
            val list = searchPageResult12.value
            for (i in 0 until list!!.size) {
                if (listId == list[i].id) {
                    list.removeAt(i)
                    break
                }
            }
            searchPageResult12.value = list
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    private fun changeWishListStatus(groupId: Int, eventKey: Boolean) {
        val list = wishListGroup.value
        list?.forEachIndexed { index, savedList: SavedList ->
            if(savedList.id == groupId) {
                if (eventKey) {
                    listGroupCount.value = listGroupCount.value?.plus(1)
                    savedList.img = listImage.value
                    savedList.wishListCount = savedList.wishListCount?.plus(1)
                } else {
                    listGroupCount.value = listGroupCount.value?.minus(1)
                    savedList.wishListCount = savedList.wishListCount?.minus(1)
                    if (savedList.wishListCount!! <= 0) {
                        savedList.img = ""
                    } else {
                        savedList.img = wishListGroupCopy.value?.get(index)?.img
                    }
                }
                savedList.isWishList = eventKey
                savedList.progress = AuthViewModel.LottieProgress.NORMAL
            }
        }
        wishListGroup.value = list
        isLoadingInProgess.value = isLoadingInProgess.value?.minus(1)
    }
    fun getWholeWishListGroup() {
        val buildMutation = GetAllWishListGroupWithoutPageQuery()
        isLoadingInProgess.value = isLoadingInProgess.value?.plus(1)
        compositeDisposable.add(dataManager.listOfWishListWithoutPage(buildMutation)
            .doOnSubscribe { setIsLoading(false) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({response ->
                try {
                    if (response.data?.getAllWishListGroup?.status == 200) {
                        duplicateid=0
                        var wishlistid=response.data?.getAllWishListGroup?.results
                        wishlistid?.forEach {
                            it?.wishListIds?.forEach {
                                if(it?.equals(listId.value) == true){
                                    duplicateid+=1
                                }
                            }
                        }
                        isLoadingInProgess.value = isLoadingInProgess.value?.minus(1)
                    } else if (response.data?.getAllWishListGroup?.status == 500) {
                            navigator.openSessionExpire("")
                    }
                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, {
                handleException(it, true)
            })
        )
    }
    fun deleteWishListGroup(groupId: Int) {
        val buildMutation = DeleteWishListGroupMutation(
            id = groupId
        )

        compositeDisposable.add(dataManager.deleteWishListGroup(buildMutation)
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    if (response.data?.deleteWishListGroup?.status == 200) {
                        retryCalled = ""
                        navigator.reloadExplore()
                        navigator.moveUpScreen()
                    } else {
                        if (response.data?.deleteWishListGroup?.errorMessage==null){
                            navigator.showError()
                        }else{
                            navigator.showToast(response.data?.deleteWishListGroup?.errorMessage.toString())
                        }
                    }
                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, { handleException(it) } )
        )
    }

    val wishListGroupResult = MutableLiveData<Listing<GetAllWishListGroupQuery.Result>>()
    lateinit var wishListGroupList: LiveData<PagedList<GetAllWishListGroupQuery.Result>>
    val wishListGroupNetworkState = wishListGroupResult.switchMap { it.networkState }!!

    fun loadwishListGroup() : LiveData<PagedList<GetAllWishListGroupQuery.Result>> {
        if (!::wishListGroupList.isInitialized) {
            wishListGroupList = MutableLiveData()
            val buildQuery = GetAllWishListGroupQuery()
            wishListGroupResult.value = dataManager.listOfWishListGroup(buildQuery, 10)
            wishListGroupList = wishListGroupResult.switchMap {
                it.pagedList
            }
        }
        return wishListGroupList
    }

    fun wishListGroupRefresh() {
        wishListGroupResult.value?.refresh?.invoke()
    }

    fun wishListGroupRetry() {
        wishListGroupResult.value?.retry?.invoke()
    }

    val wishListResult = MutableLiveData<Listing<GetWishListGroupQuery.WishList>>()
    lateinit var wishListList: LiveData<PagedList<GetWishListGroupQuery.WishList>>


    val currentPage = MutableLiveData<Int>().apply { value = 1 }
    val searchPageResult12 = MutableLiveData<ArrayList<SearchListing>>().apply { value = ArrayList() }

    fun increaseCurrentPage(page: Int) {
        currentPage.value = page
    }

    fun getSavedDetails() : Single<ApolloResponse<GetWishListGroupQuery.Data>> {
        val buildQuery = GetWishListGroupQuery(
            id = listId.value!!,
            currentPage = currentPage.value.toOptional()
        )
        return dataManager.getWishList(buildQuery)
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun setSavedData(results: List<GetWishListGroupQuery.WishList?>) {

        val list = searchPageResult12.value
        list?.addAll(parseData(results))
        searchPageResult12.value = list
    }

    private fun parseData(results: List<GetWishListGroupQuery.WishList?>): List<SearchListing> {
        val list = ArrayList<SearchListing>()
        results.forEach {
            it?.listData?.let {
                val photoList = ArrayList<Photo>()
                it.listPhotos?.forEach {
                    photoList.add(Photo(it?.id!!, it.name!!))
                }
                list.add(
                    SearchListing(
                        id = it.id!!,
                        wishListStatus = it.wishListStatus,
                        title = it.title!!,
                        roomType = it.roomType,
                        bedrooms = it.bedrooms!!,
                        additionalPrice = (it.listingData?.additionalPrice?:0.0),
                        reviewsStarRating = it.reviewsStarRating,
                        reviewsCount = it.reviewsCount,
                        personCapacity = it.personCapacity!!,
                        listPhotoName = it.listPhotoName,
                        guestBasePrice = (it.listingData?.guestBasePrice?:0.0).toInt(),
                        isListOwner = false,
                        currency = it.listingData?.currency!!,
                        coverPhoto = it.coverPhoto?:0,
                        bookingType = it.bookingType!!,
                        beds = it.beds!!,
                        city="",
                        state ="",
                        country ="",
                        basePrice = it.listingData?.basePrice!!,
                        listPhotos = photoList,
                        lat = 0.0,
                        lng = 0.0,
                        oneTotalPrice = OneTotalPrice(
                            averagePrice = 0.0,
                            nights = 0,
                            cleaningPrice = 0.0,
                            serviceFee = 0.0,
                            Total = 0.0,
                            Daytotal = 0.0,
                            discount = 0.0
                        ),
                        oneTotalpricechecked = false

                    )
                )
            }
        }
        return list
    }



}