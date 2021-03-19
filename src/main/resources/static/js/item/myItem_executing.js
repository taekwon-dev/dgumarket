import {my_items_form_view,my_items_form_close,trade_comment_form_view,trade_comment_form_close,
    my_purchased_items_form_view, my_purchased_items_form_close, products_sort_detail, purchase_sort_detail,
    request_my_item_profile, request_my_item, request_my_item_reviews, request_purchased_my_item} from '/js/item/myItem.js';

//url을 입력하여 접속할 경우 렌더링되도록 하는 함수
$(document).ready(function () {
    const user_id = location.href.split('/')[4]
    const query = location.href.split('/')[5]
    const query_separator = query.split('&')
    request_my_item_profile();
    if(query.indexOf('products') > -1){
        trade_comment_form_close();
        my_purchased_items_form_close();
        my_items_form_view(`${query_separator[0].slice(21)}`,`${query_separator[1].slice(5)}`);
        request_my_item(`${user_id}`,`${query}`,0);
    }else if(query.indexOf('reviews') > -1){
        my_items_form_close();
        my_purchased_items_form_close();
        trade_comment_form_view(`${query_separator[0].slice(13)}`);
        request_my_item_reviews(`${user_id}`,`${query}`,0)
    }else{
        my_items_form_close();
        trade_comment_form_close();
        my_purchased_items_form_view(`${query_separator[0].slice(22)}`,`${query_separator[1].slice(5)}`);
        request_purchased_my_item( `${user_id}`, `${query}`,0)
    }
})
// 판매물건, 거래후기, 구매물건UI에 따라 알맞는 view처리하는 함수
document.addEventListener('click',function (event) {
    const user_id = location.href.split('/')[4]
    switch (event.target.id) {
        case 'my_items':
            history.pushState(null,null,
                `/shop/${user_id}/products?product_set=total&sort=createDatetime,desc&size=12`)
            trade_comment_form_close();
            my_purchased_items_form_close();
            my_items_form_view(`${location.href.split('/')[5].split('&')[0].slice(21)}`
                ,`${location.href.split('/')[5].split('&')[1].slice(5)}`);
            products_page = 1
            request_my_item(`${user_id}`,`${location.href.split('/')[5]}`,0);
            break;
        case 'trade_comment':
            history.pushState(null,null,
                `/shop/${user_id}/reviews?sort=ReviewRegistrationDate,desc&size=12`)
            my_items_form_close();
            my_purchased_items_form_close();
            trade_comment_form_view(`${location.href.split('/')[5].split('&')[0].slice(13)}`);
            reviews_page = 1
            request_my_item_reviews(`${user_id}`,`${location.href.split('/')[5]}`,0)
            break;
        case 'my_purchased_items':
            history.pushState(null,null,
                `/shop/${user_id}/purchase?purchase_set=total&sort=createdDate,desc&size=12`)
            my_items_form_close();
            trade_comment_form_close();
            my_purchased_items_form_view(`${location.href.split('/')[5].split('&')[0].slice(22)}`
                ,`${location.href.split('/')[5].split('&')[1].slice(5)}`);
            purchase_page = 1
            request_purchased_my_item( `${user_id}`,`${location.href.split('/')[5]}`,0)
            break;
        // 거래후기 작성 여부 유효검사하는 함수
        case 'purchased_comment_submit':
            const after_trade_comment = document.getElementById('after_trade_comment')
            const purchased_comment_submit = document.getElementById('purchased_comment_submit')
            if(after_trade_comment.value == ''){
                alert('구매후기를 작성해주세요')
            } else{
                request_upload_trade_comment(after_trade_comment.value, purchased_comment_submit.classList[2].slice(17),event)
            }
            break;
        default:
            break;
    }
})
document.addEventListener('click',function (event) {
    // 새부정렬을 선택할 경우 해당 텍스트 색칠 처리하는 함수
    if(hasClass(event.target, 'sort_text')){
        event.target.classList.add('selected_sort')
        const sort_text = document.getElementsByClassName('sort_text')
        for (let i = 0; i < sort_text.length; i++) {
            if(event.target.id != sort_text[i].id){
                sort_text[i].classList.remove('selected_sort')
            }
        }
    }
    // 거래후기 작성 모달창을 닫을 경우 전송버튼에 넣어둔 물건번호 지우는 함수
    else if(hasClass(event.target, 'purchased_item_comment_close')){
        const purchased_comment_submit = document.getElementById('purchased_comment_submit')
        purchased_comment_submit.classList.remove(purchased_comment_submit.classList[2])
    }
})
// 거래후기 버튼UI를 통해 거래후기를 업로드하거나 조회하는 함수
document.addEventListener('click',function (event) {
    switch (event.target.innerText) {
        case '작성하기':
            const seller = document.getElementById('seller')
            const after_trade_comment = document.getElementById('after_trade_comment')
            const nav_user_nickname = document.getElementsByClassName('nav_user_nickname')
            after_trade_comment.placeholder = `${nav_user_nickname[0].innerText}님의 진심이 담긴 한마디가 ${event
                .target.classList[0].slice(7)}님에게 큰 도움이 될 수 있습니다. 또한 거래후기는 수정 삭제가 불가하므로 신중히 작성 부탁드립니다.`
            seller.innerText = `${event.target.classList[0].slice(7)}`
            const purchased_comment_submit = document.getElementById('purchased_comment_submit')
            purchased_comment_submit.classList.add(event.target.id)
            break;
        case '후기보기':
            request_view_trade_comment(event.target.id.slice(17), event)
            break;
        default:
            break;
    }
})
// 판매물품UI에서 기본정렬하는 함수
document.addEventListener('change',function (event) {
    if(event.target.id == 'my_items_select'){
        const sort_text = document.getElementsByClassName('sort_text')
        const createDatetime_desc = document.getElementById('createDatetime,desc')
        const user_id = location.href.split('/')[4]
        for (let i = 0; i < sort_text.length; i++) {
            sort_text[i].classList.remove('selected_sort')
        }
        createDatetime_desc.classList.add('selected_sort')
        products_page = 1
        switch (event.target.value) {
            case 'total':
                history.pushState(null,null,
                    `/shop/${user_id}/products?product_set=${event.target.value}&sort=createDatetime,desc&size=12`)
                request_my_item(`${user_id}`,`${location.href.split('/')[5]}`,0);
                break;
            case 'sale':
                history.pushState(null,null,
                    `/shop/${user_id}/products?product_set=${event.target.value}&sort=createDatetime,desc&size=12`)
                request_my_item(`${user_id}`,`${location.href.split('/')[5]}`,0);
                break;
            case 'sold':
                history.pushState(null,null,
                    `/shop/${user_id}/products?product_set=${event.target.value}&sort=createDatetime,desc&size=12`)
                request_my_item(`${user_id}`,`${location.href.split('/')[5]}`,0);
                break;
            default:
                break;
        }
    }
})
// 판매물품UI에서 세부정렬하는 함수
document.addEventListener('click',function (event) {
    if(location.href.indexOf('products') > -1 && hasClass(event.target, 'sort_text')){
        const user_id = location.href.split('/')[4]
        products_page = 1
        switch (document.getElementById('my_items_select').value) {
            case 'total':
                products_sort_detail(`${event.target.id}`, `${user_id}`,`${document.getElementById('my_items_select').value}`)
                break;
            case 'sale':
                products_sort_detail(`${event.target.id}`, `${user_id}`,`${document.getElementById('my_items_select').value}`)
                break;
            case 'sold':
                products_sort_detail(`${event.target.id}`,`${user_id}`,`${document.getElementById('my_items_select').value}`)
                break;
            default:
                break;
        }
    }
})
// 거래후기UI에서 기본정렬하는 함수
document.addEventListener('change',function (event) {
    if(event.target.id == 'my_comment_select'){
        const user_id = location.href.split('/')[4]
        reviews_page = 1
        switch (event.target.value) {
            case 'ReviewRegistrationDate,desc':
                history.pushState(null,null,
                    `/shop/${user_id}/reviews?sort=${event.target.value}&size=12`)
                request_my_item_reviews(`${user_id}`,`${location.href.split('/')[5]}`,0)
                break;
            case 'ReviewRegistrationDate,asc':
                history.pushState(null,null,
                    `/shop/${user_id}/reviews?sort=${event.target.value}&size=12`)
                request_my_item_reviews(`${user_id}`,`${location.href.split('/')[5]}`,0)
                break;
            default:
                break;
        }
    }
})
// 구매물품UI에서 기본정렬하는 함수
document.addEventListener('change',function (event) {
    if(event.target.id == 'my_purchase_select'){
        const sort_text = document.getElementsByClassName('sort_text')
        const createdDate_desc = document.getElementById('createdDate,desc')
        const user_id = location.href.split('/')[4]
        for (let i = 0; i < sort_text.length; i++) {
            sort_text[i].classList.remove('selected_sort')
        }
        createdDate_desc.classList.add('selected_sort')
        purchase_page = 1
        switch (event.target.value) {
            case 'total':
                history.pushState(null,null,
                    `/shop/${user_id}/purchase?purchase_set=${event.target.value}&sort=createdDate,desc&size=12`)
                request_purchased_my_item(`${user_id}`,`${location.href.split('/')[5]}`,0)
                break;
            case 'write':
                history.pushState(null,null,
                    `/shop/${user_id}/purchase?purchase_set=${event.target.value}&sort=createdDate,desc&size=12`)
                request_purchased_my_item(`${user_id}`,`${location.href.split('/')[5]}`,0)
                break;
            case 'nowrite':
                history.pushState(null,null,
                    `/shop/${user_id}/purchase?purchase_set=${event.target.value}&sort=createdDate,desc&size=12`)
                request_purchased_my_item(`${user_id}`,`${location.href.split('/')[5]}`,0)
                break;
            default:
                break;
        }
    }
})
// 구매물품UI에서 세부정렬하는 함수
document.addEventListener('click',function (event) {
    if(location.href.indexOf('purchase') > -1 && hasClass(event.target, 'sort_text')){
        const user_id = location.href.split('/')[4]
        purchase_page = 1
        switch (document.getElementById('my_purchase_select').value) {
            case 'total':
                purchase_sort_detail(`${event.target.id}`, `${user_id}`,`${document.getElementById('my_purchase_select').value}`)
                break;
            case 'write':
                purchase_sort_detail(`${event.target.id}`, `${user_id}`,`${document.getElementById('my_purchase_select').value}`)
                break;
            case 'nowrite':
                purchase_sort_detail(`${event.target.id}`, `${user_id}`,`${document.getElementById('my_purchase_select').value}`)
                break;
        }
    }
})
let products_page = 1
let reviews_page = 1
let purchase_page = 1
window.addEventListener('scroll',function () {
    if ((window.innerHeight + window.scrollY) >= document.documentElement.offsetHeight) {
        const user_id = location.href.split('/')[4]
        const query = location.href.split('/')[5]
        if(location.href.indexOf('products') > -1){
            request_my_item(`${user_id}`, query, products_page++)
        }
        else if(location.href.indexOf('reviews') > -1){
            request_my_item_reviews(user_id, query, reviews_page++)
        }
        else if(location.href.indexOf('purchase') > -1){
            request_purchased_my_item(user_id, query, purchase_page++)
        }
    }
})