package com.airhomestays.app;

import com.airbnb.epoxy.EpoxyDataBindingLayouts;
import com.airbnb.epoxy.PackageEpoxyConfig;
@EpoxyDataBindingLayouts({
        R.layout.viewholder_explore,
        R.layout.viewholder_listing,
        R.layout.viewholder_listing_header,
        R.layout.viewholder_listing_showbtn,
        R.layout.viewholder_mostviewed_listing,
        R.layout.viewholder_listing_details_carousel,
        R.layout.viewholder_listing_details_checkin,
        R.layout.viewholder_listing_details_desc,
        R.layout.viewholder_listing_details_header,
        R.layout.viewholder_listing_details_icons,
        R.layout.viewholder_build_number,
        R.layout.viewholder_listing_details_title,
        R.layout.viewholder_listing_details_similar_carousel,
        R.layout.viewholder_listing_details_list_showmore,
        R.layout.viewholder_listing_details_list_readmore,
        R.layout.viewholder_listing_details_sublist,
        R.layout.viewholder_listing_details_map,
        R.layout.viewholder_share_list,
        R.layout.viewholder_share_listing_card,
        R.layout.viewholder_listing_details_section_header,
        R.layout.viewholder_listing_details_cancellation,
        R.layout.viewholder_listing_details_photo_story,
        R.layout.viewholder_explore_listing,
        R.layout.viewholder_description_textbox,
        R.layout.viewholder_explore_search_location,
        R.layout.viewholder_divider,
        R.layout.viewholder_divider_padding,
        R.layout.viewholder_loader,
        R.layout.viewholder_filter_instantbook,
        R.layout.viewholder_filter_pricerange,
        R.layout.viewholder_filter_plus_minus,
        R.layout.viewholder_filter_plus_minus_bathroom,
        R.layout.viewholder_filter_plus_minus_bedroom,
        R.layout.viewholder_filter_checkbox,
        R.layout.viewholder_booking_date_info,
        R.layout.viewholder_booking_steper,
        R.layout.viewholder_booking_msg_host,
        R.layout.viewholder_booking_confirm_email_box,
        R.layout.viewholder_booking_upload_photo,
        R.layout.viewholder_booking_summary_listing,
        R.layout.viewholder_booking_payment_button,
        R.layout.viewholder_booking_total_cost,
        R.layout.viewholder_pricebreak_listinfo,
        R.layout.viewholder_listing_pricebreak_checkinout,
        R.layout.viewholder_pricebreak_summary,
        R.layout.viewholder_toolbar,
        R.layout.viewholder_center_text,
        R.layout.viewholder_itinerary_text_bold,
        R.layout.viewholder_itinerary_text_normal,
        R.layout.viewholder_itinerary_text_left_right,
        R.layout.viewholder_itinerary_avatar,
        R.layout.viewholder_trips_list,
        R.layout.viewholder_itinenary_listinfo,
        R.layout.viewholder_explore_search_listing_item,
        R.layout.viewholder_inbox_list_messages,
        R.layout.viewholder_inbox_info,
        R.layout.viewholder_inbox_receiver_msg,
        R.layout.viewholder_inbox_sender_msg,
        R.layout.viewholder_verified_info,
        R.layout.viewholder_user_image,
        R.layout.viewholder_user_name,
        R.layout.viewholder_user_verified_header,
        R.layout.viewholder_user_verified_text,
        R.layout.viewholder_user_normal_text,
        R.layout.viewholder_report_user_radio,
        R.layout.viewholder_profile_logout_btn,
        R.layout.viewholder_paging_retry,
        R.layout.viewholder_saved_list,
        R.layout.viewholder_saved_et,
        R.layout.viewholder_saved_placeholder,
        R.layout.viewholder_createlist_radio,
        R.layout.viewholder_saved_list_carousel,
        R.layout.viewholder_explore_no_result,
        R.layout.viewholder_saved_listing,
        R.layout.viewholder_heart_saved,
        R.layout.viewholder_country_codes,
        R.layout.viewholder_saved_list_items,
        R.layout.viewholder_trust_sections,
        R.layout.viewholder_green_button,
        R.layout.viewholder_host_cover_photo,
        R.layout.viewholder_list_et,
        R.layout.viewholder_guest_req,
        R.layout.viewholder_custom_toggle,
        R.layout.viewholder_list_tv,
        R.layout.viewholder_host_plus_minus,
        R.layout.viewholder_gray_textview,
        R.layout.viewholder_radio_text_sub,
        R.layout.viewholder_list_num_et,
        R.layout.viewholder_option_text,
        R.layout.viewholder_add_listing,
        R.layout.viewholder_host_final_page,
        R.layout.viewholder_host_step_one,
        R.layout.viewholder_host_bottom_options,
        R.layout.viewholder_host_address_et,
        R.layout.viewholder_bathroomtype,
        R.layout.viewholder_host_checkbox,
        R.layout.viewholder_host_profile,
        R.layout.viewholder_center_text_placeholder,
        R.layout.viewholder_host_select_country,
        R.layout.viewholder_payout_account_details,
        R.layout.viewholder_review_detail,
        R.layout.view_holder_add_bank_detail,
        R.layout.viewholder_payout_account_info,
        R.layout.viewholder_payout_type,
        R.layout.viewholder_payout_choose_how_we_pay,
        R.layout.viewholder_payout_paypal_details,
        R.layout.viewholder_calendar_listing,
        R.layout.viewholder_host_inital_below,
        R.layout.viewholder_list_desc_et,
        R.layout.viewholder_manage_list,
        R.layout.viewholder_host_trips_list,
        R.layout.viewholder_placeholder_addphotos,
        R.layout.viewholder_empty_model,
        R.layout.viewholder_profile_header,
        R.layout.viewholder_profile_lists,
        R.layout.viewholder_profile_list_bold,
        R.layout.viewholder_feedback,
        R.layout.viewholder_webview,
        R.layout.viewholder_pre_approved,
        R.layout.viewholder_specialprice_et,
        R.layout.viewholder_powered_by_google,
        R.layout.viewholder_describe_experience,
        R.layout.viewholder_listing_details_readreview,
        R.layout.viewholder_listing_details_reviews,
        R.layout.viewholder_listing_review_header,
        R.layout.viewholder_booking_review_pay_fee,
        R.layout.viewholder_review_info,
        R.layout.viewholder_pending_review_info,
        R.layout.viewholder_display_review_listing,
        R.layout.viewholder_overall_rating,
        R.layout.viewholder_review_header,
        R.layout.viewholder_listing_details_review_list,
        R.layout.viewholder_payment_stripe_button,
        R.layout.viewholder_paypal_payment_buttton,
        R.layout.viewholder_select_payment_type,
        R.layout.viewholder_logout,
        R.layout.viewholder_header,
        R.layout.viewholder_listing_details_desc_2,
        R.layout.viewholder_host_step_one_icon,
        R.layout.viewholder_listing_details_show_all,
        R.layout.viewholder_header_small,
        R.layout.viewholder_listing_details_checkin_out,
        R.layout.viewholder_user_heading_small,
        R.layout.viewholder_user_normal_text_blue,
        R.layout.viewholder_add_listing_photos,

        R.layout.viewholder_select_payment_currency,
        R.layout.viewholder_popular_location_item,
        R.layout.viewholder_divider_no_padding,
        R.layout.viewholder_become_a_host_banner,
        R.layout.viewholder_view_padding,
        R.layout.viewholder_calendar_available_header,
        R.layout.viewholder_calendar_date_header,
        R.layout.viewholder_itinerary_text,
        R.layout.viewholder_receipt_text_bold,
        R.layout.viewholder_review_pay_checkin,
        R.layout.viewholder_review_and_pay_span_text,
        R.layout.viewholder_curve_bg,
        R.layout.viewholder_filter_date_guest,
        R.layout.viewholder_user_verified_status,
        R.layout.viewholder_divider_padding_top,
        R.layout.viewholder_user_name2,
        R.layout.viewholder_profile_review_info,
        R.layout.shimmer_listing,
        R.layout.viewholder_bg_bottomsheet,
        R.layout.viewholder_itinerary_text_left_right_black,
        R.layout.viewholder_step_one_chips,
        R.layout.viewholder_step_two_chips,
        R.layout.viewholder_filter_plus_minus_guest,
        R.layout.viewholder_step_three_chips,
        R.layout.viewholder_search_carousel,
        R.layout.viewholder_indicator,
        R.layout.viewholder_cancellation_policy,
        R.layout.viewholder_tips,
        R.layout.viewholder_amenities,
        R.layout.viewholder_filter_plus_minus_dropdown,
        R.layout.viewholder_navigate,
        R.layout.viewholder_discount,
        R.layout.viewholder_divider_list_tv,
        R.layout.viewholder_steps,
        R.layout.viewholder_bg_bottomsheet_currency,
        R.layout.viewholder_listing_details_beds,
        R.layout.viewholder_search_listing_hometype,
        R.layout.viewholder_display_total_price,
        R.layout.viewholder_pricebreakdown_bottomsheet,
        R.layout.viewholder_item_tab,
        R.layout.viewholder_select_dates

})

@PackageEpoxyConfig(
        requireAbstractModels = true,
        requireHashCode = true,
        implicitlyAddAutoModels = true
)

interface EpoxyBindingInfo {

}
