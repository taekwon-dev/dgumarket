import {hasClass} from '/js/module/module.js';

export function my_items_form_view(product_set, sort) {
    const my_items = document.getElementById('my_items')
    my_items.style.borderBottom = '3px solid rgb(29, 161, 242)'
    my_items.style.color = 'rgb(29, 161, 242)';
    const sort_detail_text = document.getElementById(`${sort}`)
    sort_detail_text.classList.add('selected_sort')
    const my_items_select = document.getElementById('my_items_select')
    my_items_select.value = `${product_set}`
    const my_items_form = document.getElementById('my_items_form')
    my_items_form.classList.remove('hidden')
}
// '나의 중고물품' 리스트 닫는 함수
export function my_items_form_close() {
    const my_items_form = document.getElementById('my_items_form')
    my_items_form.classList.add('hidden')
    const my_items = document.getElementById('my_items')
    my_items.style.borderBottom = '1px solid transparent'
    my_items.style.color = '#868e96';
    const sort_text = document.getElementsByClassName('sort_text')
    for (let i = 0; i < sort_text.length; i++) {
        sort_text[i].classList.remove('selected_sort')
    }
}
// '거래 후기' 보여주는 함수
export function trade_comment_form_view(sort) {
    const trade_comment = document.getElementById('trade_comment')
    trade_comment.style.borderBottom = '3px solid rgb(29, 161, 242)'
    trade_comment.style.color = 'rgb(29, 161, 242)';
    const my_comment_select = document.getElementById('my_comment_select')
    my_comment_select.value = `${sort}`
    const trade_comment_form = document.getElementById('trade_comment_form')
    trade_comment_form.classList.remove('hidden')
}
// '거래 후기' 닫는 함수
export function trade_comment_form_close() {
    const trade_comment_form = document.getElementById('trade_comment_form')
    trade_comment_form.classList.add('hidden')
    const trade_comment = document.getElementById('trade_comment')
    trade_comment.removeAttribute('style')
}
// '나의 구매물품'을 보여주는 함수
export function my_purchased_items_form_view(purchase_set, sort) {
    const my_purchased_items = document.getElementById('my_purchased_items')
    my_purchased_items.style.borderBottom = '3px solid rgb(29, 161, 242)'
    my_purchased_items.style.color = 'rgb(29, 161, 242)';
    const sort_detail_text = document.getElementById(`${sort}`)
    sort_detail_text.classList.add('selected_sort')
    const my_purchase_select = document.getElementById('my_purchase_select')
    my_purchase_select.value = `${purchase_set}`
    const my_purchased_items_form = document.getElementById('my_purchased_items_form')
    my_purchased_items_form.classList.remove('hidden')
}
// '나의 구매물품'을 닫는 함수
export function my_purchased_items_form_close() {
    const my_purchased_items_form = document.getElementById('my_purchased_items_form')
    my_purchased_items_form.classList.add('hidden')
    const my_purchased_items = document.getElementById('my_purchased_items')
    my_purchased_items.removeAttribute('style')
    const sort_text = document.getElementsByClassName('sort_text')
    for (let i = 0; i < sort_text.length; i++) {
        sort_text[i].classList.remove('selected_sort')
    }
}
// 메뉴 중에서 업로드된 판매물품 or 거래후기 or 구매한 물품이 없을 경우 디폴트 값을 나타내는 함수(서버와 통신하는 코드에 넣어준다.)
export function empty_contents() {
    let empty_text;
    if(location.href.indexOf('products') > -1){
        empty_text =
            `<span class="not_upload">아직 업로드한 중고물품이 없습니다. <i class="d-flex flex-wrap align-content-center far fa-sad-tear"></i></span>`;
    }
    else if(location.href.indexOf('reviews') > -1){
        empty_text =
            `<span class="not_upload">아직 등록된 거래후기가 없습니다. <i class="d-flex flex-wrap align-content-center far fa-sad-tear"></i></span>`;
    }
    else if(location.href.indexOf('purchase') > -1){
        empty_text =
            `<span class="not_upload">아직 구매하신 중고물품이 없습니다. <i class="d-flex flex-wrap align-content-center far fa-sad-tear"></i></span>`;
    }
    const empty_span = document.createElement('span')
    empty_span.innerHTML = empty_text
    return empty_span;
}
// 업로드 날짜 파싱하는 함수
export function upload_date_parsing(upload_date) {
    if(upload_date != null){
        const date = new Date();
        const mm =  date.getMonth() < 9 ? `0${date.getMonth()+1}` : `${date.getMonth()+1}`;
        const dd = date.getDate() < 10 ? `0${date.getDate()}` : `${date.getDate()}`
        let get_date;
        if(upload_date.slice(0,13) == `${date.getFullYear()}-${mm}-${dd}T${date.getHours()}`){
            get_date = `${Number(date.getMinutes())-Number(upload_date.slice(14,16))}분전`
        }
        else if(upload_date.slice(0,10) == `${date.getFullYear()}-${mm}-${dd}`){
            get_date = `${Number(date.getHours())-Number(upload_date.slice(11,13))}시간전`
        }else if(upload_date.slice(0,7) == `${date.getFullYear()}-${mm}`){
            get_date = `${Number(dd)-Number(upload_date.slice(8,10))}일전`
        }else if(upload_date.slice(0,4) == `${date.getFullYear()}`){
            get_date = `${Number(mm)-Number(upload_date.slice(5,7))}달전`
        }else{
            get_date = `${Number(date.getFullYear())-Number(upload_date.slice(0,4))}년전`
        }
        return get_date;
    }
}
// 판매물품 세부정렬로 정보를 필터링하는 함수
export function products_sort_detail(e, user_id, products_set) {
    switch (e) {
        case 'createDatetime,desc':
            history.pushState(null,null,
                `/shop/${user_id}/products?product_set=${products_set}&sort=${e}&size=12`)
            request_my_item(`${user_id}`,`${location.href.split('/')[5]}`,0);
            break;
        case 'createDatetime,asc':
            history.pushState(null,null,
                `/shop/${user_id}/products?product_set=${products_set}&sort=${e}&size=12`)
            request_my_item(`${user_id}`,`${location.href.split('/')[5]}`,0);
            break;
        case 'likeNums,desc':
            history.pushState(null,null,
                `/shop/${user_id}/products?product_set=${products_set}&sort=${e}&size=12`)
            request_my_item(`${user_id}`,`${location.href.split('/')[5]}`,0);
            break;
        case 'chatroomNums,desc':
            history.pushState(null,null,
                `/shop/${user_id}/products?product_set=${products_set}&sort=${e}&size=12`)
            request_my_item(`${user_id}`,`${location.href.split('/')[5]}`,0);
            break;
        case 'price,asc':
            history.pushState(null,null,
                `/shop/${user_id}/products?product_set=${products_set}&sort=${e}&size=12`)
            request_my_item(`${user_id}`,`${location.href.split('/')[5]}`,0);
            break;
        case 'price,desc':
            history.pushState(null,null,
                `/shop/${user_id}/products?product_set=${products_set}&sort=${e}&size=12`)
            request_my_item(`${user_id}`,`${location.href.split('/')[5]}`,0);
            break;
        default:
            break;
    }
}
// 구매물품 세부정렬로 정보를 필터링하는 함수
export function purchase_sort_detail(e, user_id, purchase_set) {
    switch (e) {
        case 'createdDate,desc':
            history.pushState(null,null,
                `/shop/${user_id}/purchase?purchase_set=${purchase_set}&sort=${e}&size=12`)
            request_purchased_my_item(`${user_id}`,`${location.href.split('/')[5]}`,0);
            break;
        case 'createdDate,asc':
            history.pushState(null,null,
                `/shop/${user_id}/purchase?purchase_set=${purchase_set}&sort=${e}&size=12`)
            request_purchased_my_item(`${user_id}`,`${location.href.split('/')[5]}`,0);
            break;
        default:
            break;
    }
}
// 프로필 정보를 렌더링하는 함수
function my_item_profile(s_res) {
    const my_nickname = document.getElementById('my_nickname')
    const my_picture = document.getElementById('my_picture')
    my_nickname.innerText = `${s_res.data.nickName}`
    my_picture.src = `${s_res.data.profileImageDir}`
    for (let i = 0; i < s_res.data.productCategories.length; i++) {
        const my_info2 = document.getElementById('my_info2')
        const span_categories = document.createElement('span')
        span_categories.setAttribute('class','my_category_text')
        span_categories.innerText = `#${s_res.data.productCategories[i].category_name}`
        my_info2.appendChild(span_categories)
    }
}
// 판매물품에 대한 정보를 동적으로 생성하는 함수
function my_item(s_res) {
    if ((window.innerHeight + window.scrollY) < document.documentElement.offsetHeight) {
        const sale_list_form = document.getElementById('sale_list_form')
        while (sale_list_form.hasChildNodes()){
            sale_list_form.removeChild(sale_list_form.firstChild)
        }
    }
    const my_item_count = document.getElementById('my_item_count')
    my_item_count.innerText = `${s_res.data.total_size}`
    for (let i = 0; i < s_res.data.productsList.length; i++) {
        const item_div = document.createElement('div')
        item_div.setAttribute('class','item_form col-6 col-sm-4 col-md-4 col-lg-3 col-xl-3')
        item_div.innerHTML = `
                    <div class="item_picture_form">
                        <img src="${s_res.data.productsList[i].thumbnailImg}" href="/shop/item/onePick" 
                            id="my_itme_no${s_res.data.productsList[i].id}" class="move_onePick item_picture" alt="">
                    </div>
                    <div class="item_text">
                        <div><a class="move_onePick item_title" 
                            href="/shop/item/onePick">${s_res.data.productsList[i].title}</a></div>
                        <div><a class="move_onePick item_price" 
                            href="/shop/item/onePick">${s_res.data.productsList[i].price.slice(1)}원</a></div>
                    </div>
                    <div class="item_info_good_view">
                        <span class="good_button"><i class="fas fa-heart"></i></span>
                        <span>${s_res.data.productsList[i].likeNums}</span>
                        <span>∙</span>
                        <span class="chat_count"><i class="fas fa-comment-dots"></i></span>
                        <span>${s_res.data.productsList[i].chatroomNums}</span>
                        <span class="section">∙</span>
                        <span class="upload_time">${upload_date_parsing(s_res.data.productsList[i].uploadDatetime)}</span>
                    </div>`
        sale_list_form.appendChild(item_div)
    }
}
// 거래후기 정보를 동적생성하는 함수
function my_item_reviews(s_res) {
    if ((window.innerHeight + window.scrollY) < document.documentElement.offsetHeight) {
        const comment_list_form = document.getElementById('comment_list_form')
        while (comment_list_form.hasChildNodes()){
            comment_list_form.removeChild(comment_list_form.firstChild)
        }
    }
    const trade_comment_count = document.getElementById('trade_comment_count')
    trade_comment_count.innerText = `${s_res.data.total_size}`
    for (let i = 0; i < s_res.data.review_list.length; i++) {
        const review_div = document.createElement('div')
        const hr = document.createElement('hr')
        review_div.setAttribute('class','d-flex align-items-center')
        review_div.innerHTML = `
                    <div class="d-flex">
                        <img src="${s_res.data.review_list[i].review_user_icon}" href="/shop/item/myItem" 
                            class="rounded-circle other_user_picture move_myItem" alt="">
                    </div>
                    <div class="d-block other_user_info">
                        <div href="/shop/item/myItem" class="other_user_nickname move_myItem">
                            ${s_res.data.review_list[i].review_nickname}
                        </div>
                        <div class="other_user_comment">
                            ${s_res.data.review_list[i].review_comment}
                        </div>
                        <div class="other_user_comment_uploadTime">
                            ${upload_date_parsing(s_res.data.review_list[i].review_date)}
                        </div>
                    </div>`
        comment_list_form.appendChild(review_div)
        comment_list_form.appendChild(hr)
    }
}
// 구매물품에 대한 정보를 동적생성하는 함수
function purchased_my_item(s_res) {
    if ((window.innerHeight + window.scrollY) < document.documentElement.offsetHeight) {
        const my_purchased_item_list_form = document.getElementById('my_purchased_item_list_form')
        while (my_purchased_item_list_form.hasChildNodes()){
            my_purchased_item_list_form.removeChild(my_purchased_item_list_form.firstChild)
        }
    }
    const my_purchased_items_count = document.getElementById('my_purchased_items_count')
    my_purchased_items_count.innerText = `${s_res.data.total_size}`
    for (let i = 0; i < s_res.data.purchase_product_list.length; i++) {
        const purchase_div = document.createElement('div')
        const hr = document.createElement('hr')
        const get_review = s_res.data.purchase_product_list[i]._review == false ? '작성하기' : '후기보기'
        const get_modal_id = get_review == '작성하기' ? 'write_purchased_item_comment' : 'view_purchased_item_comment'
        purchase_div.setAttribute('class', 'd-flex align-items-center')
        purchase_div.innerHTML = `
                    <div>
                        <img src="${s_res.data.purchase_product_list[i].purchase_product_img}" href="/shop/item/onePick" 
                           id="purchase_no${s_res.data.purchase_product_list[i].purchase_product_id}" 
                           class="rounded-circle my_purchased_item_picture move_onePick" alt="">
                    </div>
                    <div class="my_purchased_item_info">
                        <div class="clearfix">
                            <span href="/shop/item/onePick" class="my_purchased_item_title move_onePick float-left">
                                ${s_res.data.purchase_product_list[i].purchase_title}
                            </span>
                            <!--큰 디바이스에서 보이도록 설정-->
                            <span class="big_divice_right_info float-right">
                                <span class="good_button"><i class="fas fa-heart"></i></span>
                                <span class="">${s_res.data.purchase_product_list[i].purchase_like_num}</span>
                                <span>∙</span>
                                <span class="chat_count"><i class="fas fa-comment-dots"></i></span>
                                <span class="">${s_res.data.purchase_product_list[i].purchase_chat_num}</span>
                                <span>∙</span>
                                <span>${upload_date_parsing(s_res.data.purchase_product_list[i].purchase_date)}</span>
                            </span>
                        </div>
                        <div class="my_purchased_item_price">
                            ${s_res.data.purchase_product_list[i].purchase_price.slice(1)}원
                        </div>
                        <div>
                            <span class="my_purchased_item_comment_status">거래후기:&nbsp;</span>
                            <span id="purchased_item_no${s_res.data.purchase_product_list[i].purchase_product_id}" 
                                class="seller_${s_res.data.purchase_product_list[i].purchase_seller_nickname} 
                                my_purchased_item_comment badge badge-pill badge-primary"
                                data-toggle="modal" data-target="#${get_modal_id}">${get_review}</span>
                        </div>
                        <!-- 작은 디바이스의 경우 보이도록 설정-->
                        <div class="small_divice_right_info">
                            <span class="good_button"><i class="fas fa-heart"></i></span>
                            <span>${s_res.data.purchase_product_list[i].purchase_like_num}</span>
                            <span>∙</span>
                            <span class="chat_count"><i class="fas fa-comment-dots"></i></span>
                            <span>${s_res.data.purchase_product_list[i].purchase_chat_num}</span>
                            <span>∙</span>
                            <span>${upload_date_parsing(s_res.data.purchase_product_list[i].purchase_date)}</span>
                        </div>
                    </div>`
        my_purchased_item_list_form.appendChild(purchase_div)
        my_purchased_item_list_form.appendChild(hr)
    }
}
// 해당 유저의 프로필 조회를 요청하는 함수
export function request_my_item_profile() {
    if(location.href.indexOf('products') > -1 || location.href.indexOf('reviews') > -1 || location.href.indexOf('purchase') > -1){
        const user_id = location.href.split('/')[4]
        const reqPromise = fetch(`/api/shop/${user_id}/profile`, {
            method: 'GET',
            headers : {Accept : 'application/json'}
        })
        reqPromise.then(res => {
            if (res.status >= 200 && res.status < 300){
                console.log("'나의 거래 정보' 상단 프로필 정보 요청 성공")
                return res.json();
            }else{
                console.log("'나의 거래 정보' 상단 프로필 정보 요청 실패")
                return Promise.reject(new Error(res.status))
            }
        })
            .then(data => {
                console.log(data);
                my_item_profile(data)
            })
            .catch(error => {
                console.log(error)
            })
    }
}
// 해당 유저의 판매물품 조회를 요청하는 함수
export function request_my_item(user_id, query, page) {
    if(location.href.indexOf('products') > -1) {
        const reqPromise = fetch(`/api/shop/${user_id}/${query}&page=${page}`, {
            method: 'GET',
            headers : {Accept : 'application/json'}
        })
        reqPromise.then(res => {
            if (res.status >= 200 && res.status < 300){
                console.log("'나의 거래 정보' 판매물품 정보 요청 성공")
                return res.json();
            }else{
                console.log("'나의 거래 정보' 판매물품 정보 요청 실패")
                if(res.status == '403'){
                    alert('해당 유저를 차단 혹은 차단되어 현재 페이지를 이용하실 수 없습니다.')
                }
                return Promise.reject(new Error(res.status))
            }
        })
            .then(data => {
                console.log(data)
                if(!document.getElementById('sale_list_form').hasChildNodes()
                    && Number(data.data.page_size) == 0){
                    document.getElementById('my_item_count').innerText = `${data.data.total_size}`
                    document.getElementById('sale_list_form').appendChild(empty_contents())
                }else if(Number(data.data.page_size) > 0) {
                    my_item(data)
                }
                console.log(page)
            })
            .catch(error => {
                console.log(error)
            })
    }
}
// 해당 유저의 판매물품에 대한 거래후기 조회를 요청하는 함수
export function request_my_item_reviews(user_id, query, page) {
    if(location.href.indexOf('reviews') > -1){
        const reqPromise = fetch(`/api/shop/${user_id}/${query}&page=${page}`, {
            method: 'GET',
            headers : {Accept : 'application/json'}
        })
        reqPromise.then(res => {
            if (res.status >= 200 && res.status < 300){
                console.log("'나의 거래 정보' 거래후기 정보 요청 성공")
                return res.json();
            }else{
                console.log("'나의 거래 정보' 거래후기 정보 요청 실패")
                if(res.status == '403'){
                    alert('해당 유저를 차단 혹은 차단되어 현재 페이지를 이용하실 수 없습니다.')
                }
                return Promise.reject(new Error(res.status))
            }
        })
            .then(data => {
                console.log(data)
                if(!document.getElementById('comment_list_form').hasChildNodes()
                    && Number(data.data.page_size) == 0){
                    document.getElementById('trade_comment_count').innerText = `${data.data.total_size}`
                    document.getElementById('comment_list_form').appendChild(empty_contents())
                }else if(Number(data.data.page_size) > 0) {
                    my_item_reviews(data)
                }
                console.log(page)
            })
            .catch(error => {
                console.log(error)
            })
    }
}
// 해당 유저가 구매한 중고물품 조회를 요청하는 함수
export function request_purchased_my_item(user_id, query, page) {
    if(location.href.indexOf('purchase') > -1) {
        const reqPromise = fetch(`/api/shop/${user_id}/${query}&page=${page}`, {
            method: 'GET',
            headers : {Accept : 'application/json'}
        })
        reqPromise.then(res => {
            if (res.status >= 200 && res.status < 300){
                console.log("'나의 거래 정보' 구매물품 정보 요청 성공")
                return res.json();
            }else{
                console.log("'나의 거래 정보' 구매물품 정보 요청 실패")
                if(res.status == '403'){
                    alert('해당 유저를 차단 혹은 차단되어 현재 페이지를 이용하실 수 없습니다.')
                }
                return Promise.reject(new Error(res.status))
            }
        })
            .then(data => {
                console.log(data)
                if(!document.getElementById('my_purchased_item_list_form').hasChildNodes()
                    && Number(data.data.page_size) == 0){
                    document.getElementById('my_purchased_items_count').innerText = `${data.data.total_size}`
                    document.getElementById('my_purchased_item_list_form').appendChild(empty_contents())
                }else if(Number(data.data.page_size) > 0) {
                    purchased_my_item(data)
                }
                console.log(page)
            })
            .catch(error => {
                console.log(error)
            })
    }
}