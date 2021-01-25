// '나의 중고물품' 리스트 보여주는 함수
function my_items_form_view() {
    const my_items_form = document.getElementById('my_items_form')
    const my_items = document.getElementById('my_items')
    my_items.style.borderBottom = '3px solid #007bff'
    my_items.style.color = '#007bff';
    my_items_form.classList.remove('hidden')
}
// '나의 중고물품' 리스트 닫는 함수
function my_items_form_close() {
    const my_items_form = document.getElementById('my_items_form')
    const my_items = document.getElementById('my_items')
    my_items.removeAttribute('style')
    my_items_form.classList.add('hidden')

}
// '거래 후기' 보여주는 함수
function trade_comment_form_view() {
    const trade_comment_form = document.getElementById('trade_comment_form')
    const trade_comment = document.getElementById('trade_comment')
    trade_comment.style.borderBottom = '3px solid #007bff'
    trade_comment.style.color = '#007bff';
    trade_comment_form.classList.remove('hidden')
}
// '거래 후기' 닫는 함수
function trade_comment_form_close() {
    const trade_comment_form = document.getElementById('trade_comment_form')
    const trade_comment = document.getElementById('trade_comment')
    trade_comment.removeAttribute('style')
    trade_comment_form.classList.add('hidden')

}
// '나의 구매물품'을 보여주는 함수
function my_purchased_items_form_view() {
    const my_purchased_items_form = document.getElementById('my_purchased_items_form')
    const my_purchased_items = document.getElementById('my_purchased_items')
    my_purchased_items.style.borderBottom = '3px solid #007bff'
    my_purchased_items.style.color = '#007bff';
    my_purchased_items_form.classList.remove('hidden')
}
// '나의 구매물품'을 닫는 함수
function my_purchased_items_form_close() {
    const my_purchased_items_form = document.getElementById('my_purchased_items_form')
    const my_purchased_items = document.getElementById('my_purchased_items')
    my_purchased_items.removeAttribute('style')
    my_purchased_items_form.classList.add('hidden')
}
// 메뉴 중에서 업로드된 거래후기나 사용자가 구매한 물품이 없을 경우 디폴트 값을 나타내는 함수(서버와 통신하는 코드에 넣어준다.)
function empty_contents() {
    let empty_text;
    const trade_comment_form = document.getElementById('trade_comment_form')
    const my_purchased_items_form = document.getElementById('my_purchased_items_form')
    if(trade_comment_form){
        empty_text = `<span id="not_upload_text" class="not_upload">아직 등록된 거래후기가 없습니다.&nbsp;</span>`;
    }
    if(my_purchased_items_form){
        empty_text = `<span id="not_upload_text" class="not_upload">아직 구매하신 중고물품이 없습니다.&nbsp;</span>`;
    }
    const empty_div = document.createElement('div')
    const empty_content = `${empty_text}<i id="not_upload_icon" class="not_upload far fa-sad-tear"></i>`
    empty_div.innerHTML = empty_content
    return empty_div
}


document.addEventListener('click',function (event) {
    if(event.target.id == 'my_items'){
        my_items_form_view();
        trade_comment_form_close();
        my_purchased_items_form_close();
    }
    if(event.target.id == 'trade_comment'){
        trade_comment_form_view();
        my_items_form_close();
        my_purchased_items_form_close();
    }
    if(event.target.id == 'my_purchased_items'){
        my_purchased_items_form_view();
        my_items_form_close();
        trade_comment_form_close();
    }
})