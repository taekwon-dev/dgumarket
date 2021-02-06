import {image_extension_filter} from '/js/module/module.js';

// 중고물품 사진 업로드 버튼의 높이를 가로의 길이에 맞게 조절해주는 함수
function upLoad_picture_button_height() {
    const upLoad_picture_button = document.getElementById('upLoad_picture_button')
    if(upLoad_picture_button){
        upLoad_picture_button.style.height = (upLoad_picture_button.clientWidth)+ 'px'
    }
}
// 업로드할 중고물품 사진을 5장까지만 등록하도록 제한하는 함수
function upload_picture_limit(event){
    const upLoad_picture = document.getElementsByClassName('upLoad_picture')
    const wrong_upload = document.getElementsByClassName('wrong_upload')
    if (upLoad_picture.length < 5) {
        const upLoad_picture_div = document.createElement('div')
        upLoad_picture_div.setAttribute('class', 'col-6 col-sm-4 col-md-3 col-lg-2 upLoad_picture_select')
        const upload_picture_content = `
            <img src= ${event.target.result} class="rounded upLoad_picture">
            <i class="fas fa-times-circle rounded-circle delete_upLoad_picture"></i>`
        upLoad_picture_div.innerHTML = upload_picture_content
        const upLoad_picture_form = document.getElementById('upLoad_picture_form')
        upLoad_picture_form.appendChild(upLoad_picture_div)
        upload_picture_height()
        // 업로드 후 인풋의 파일명을 초기화한다.
        upLoad_picture_file.value = "";
        // 해당 중고물품 사진을 추가한 만큼 사진수량 카운트(더하기)
        const limit_counter = upLoad_picture.length
        const upLoad_picture_limit_counter = document.getElementById('upLoad_picture_limit_counter')
        upLoad_picture_limit_counter.innerText = limit_counter
        wrong_upload[0].innerHTML = "";
    }
    // 업로드할 중고물품 사진을 5장 초과하여 추가하고자 할 시 경고창 팝업
    else if (upLoad_picture.length >= 5) {
        wrong_upload[0].innerHTML = "<i class=\"fas fa-times-circle\"></i>"
            +"<span>"+" 중고물품 이미지는 최대 5장까지 업로드할 수 있습니다."+"</span>";
    }
}

// 중고물품 이미지 미리보기 UI의 높이 자동 조절하는 함수
function upload_picture_height() {
    const upLoad_picture_button = document.getElementById('upLoad_picture_button')
    const upLoad_picture = document.getElementsByClassName('upLoad_picture')
    if (upLoad_picture_button){
        for(let i = 0; i < upLoad_picture.length; i++){
            upLoad_picture[i].style.height = (upLoad_picture_button.clientWidth)+'px'
        }
    }
}

// 해당 중고물품 사진 삭제 버튼 클릭 시 삭제하는 함수
function upload_picture_delete(){
    const delete_upLoad_picture = document.getElementsByClassName('delete_upLoad_picture')
    const upLoad_picture = document.getElementsByClassName('upLoad_picture')
    const wrong_upload = document.getElementsByClassName('wrong_upload')
    for (let i = 0; i < delete_upLoad_picture.length; i++) {
        delete_upLoad_picture[i].addEventListener('click',function(event) {
            if(event.target.parentNode.parentNode){
                event.target.parentNode.parentNode.removeChild(event.target.parentNode)
            }
            // 해당 중고물품 사진을 삭제한 만큼 사진수량 카운트(뺴기)
            const limit_counter = upLoad_picture.length
            const upLoad_picture_limit_counter = document.getElementById('upLoad_picture_limit_counter')
            upLoad_picture_limit_counter.innerText = limit_counter
            wrong_upload[0].innerHTML = "";
        })
    }
}

// 가격 입력 시 1000단위마다 콤마 붙고 첫자리 숫자 0 제거하는 함수
function monetary_comma(price){
    const format_zero = /[0-9]+/
    price = price.replace(/[^0-9]/g,'');
    price = price.replace(/,/g,'')
    if (format_zero.test(price[1])){
        price = price.replace(/(^0+)/,"")
    }
    upLoad_price.value = "￦"+price.replace(/\B(?=(\d{3})+(?!\d))/g, ",")
}

// 가격 입력 시 0일 경우 무료나눔 태그 보이게 하기
function upload_price_free(price){
    const free_share = document.getElementById('free_share')
    if (price == '￦0'){
        free_share.classList.remove('hidden')
    }else{
        free_share.classList.add('hidden')
    }
}

// 중고물품 업로드시 필수항목 전부 입력완료했는지 유효검사하는 함수
function upLoad_check(){
    const upLoad_picture = document.getElementsByClassName('upLoad_picture')
    const wrong_upload = document.getElementsByClassName('wrong_upload')
    const upLoad_title = document.getElementById('upLoad_title')
    const upLoad_category = document.getElementById('upLoad_category')
    const upLoad_price_negotiation = document.getElementById('upLoad_price_negotiation')
    const upLoad_quality = document.getElementById('upLoad_quality')
    const upLoad_tradingMethod = document.getElementById('upLoad_tradingMethod')
    const upLoad_comment = document.getElementById('upLoad_comment')
    if(upLoad_picture.length < 1){
        wrong_upload[0].innerHTML = "<i class=\"fas fa-times-circle\"></i>"
            +"<span>"+" 중고물품 이미지는 최소 1장 이상 업로드가 필요합니다."+"</span>";
    }else{wrong_upload[0].innerHTML = "";}

    if(upLoad_title.value.length < 1){
        wrong_upload[1].innerHTML = "<i class=\"fas fa-times-circle\"></i>"
            +"<span>"+" 중고물품명을 입력하세요"+"</span>";
    }else{wrong_upload[1].innerHTML = "";}

    if(upLoad_category.value == ""){
        wrong_upload[2].innerHTML = "<i class=\"fas fa-times-circle\"></i>"
            +"<span>"+" 카테고리를 선택하세요"+"</span>";
    }else{wrong_upload[2].innerHTML = "";}

    if(upLoad_price_negotiation.value == ""){
        wrong_upload[3].innerHTML = "<i class=\"fas fa-times-circle\"></i>"
            +"<span>"+" 가격 조정 여부를 선택해주세요"+"</span>";
    }else{wrong_upload[3].innerHTML = "";}

    if(upLoad_price.value.length < 2){
        wrong_upload[4].innerHTML = "<i class=\"fas fa-times-circle\"></i>"
            +"<span>"+" 가격을 입력해주세요"+"</span>";
    }else{wrong_upload[4].innerHTML = "";}

    if(upLoad_quality.value == ""){
        wrong_upload[5].innerHTML = "<i class=\"fas fa-times-circle\"></i>"
            +"<span>"+" 중고물품의 상태를 선택해주세요"+"</span>";
    }else{wrong_upload[5].innerHTML = "";}

    if(upLoad_tradingMethod.value == ""){
        wrong_upload[6].innerHTML = "<i class=\"fas fa-times-circle\"></i>"
            +"<span>"+" 거래방식을 선택해주세요"+"</span>";
    }else{wrong_upload[6].innerHTML = "";}

    if(upLoad_comment.value.length < 1){
        wrong_upload[7].innerHTML = "<i class=\"fas fa-times-circle\"></i>"
            +"<span>"+" 중고물품에 대해 간략하게 소개해주세요"+"</span>";
    }else{wrong_upload[7].innerHTML = "";}
}

window.addEventListener('resize', function(){
    upLoad_picture_button_height()
    upload_picture_height()
})

document.addEventListener('DOMContentLoaded',function(){
    upLoad_picture_button_height()
})

document.addEventListener('change',function (event) {
    if(event.target.id == 'upLoad_picture_file'){
        for (let i = 0; i < event.target.files.length; i++){
            console.log(event.target.files[i])
            if(image_extension_filter(event,event.target.files[i])){
                const reader = new FileReader();
                reader.readAsDataURL(event.target.files[i])
                reader.onload = function(event){
                    upload_picture_limit(event)
                    upload_picture_delete()
                }
            }
        }
    }
})

document.addEventListener('keyup',function (event){
    if(event.target.id == 'upLoad_price'){
        monetary_comma(event.target.value);
        upload_price_free(event.target.value);
    }
})

document.addEventListener('click',function (event) {
    if(event.target.id == 'upLoad_success'){upLoad_check();}
})