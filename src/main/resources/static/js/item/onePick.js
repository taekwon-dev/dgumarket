// 동적 생성된 해당 class가 있는 태그를 클릭하였을 때 이벤트 실행되도록 해주는 함수
import {hasClass} from '/js/module/module.js';
const nav_hidden = document.querySelector('nav')
const footer_hidden = document.querySelector('footer')
const body_style = document.querySelector('body');
const main_content = document.querySelector('main');
// 해당 슬라이드 이미지 클릭시 큰 사이즈의 이미지 슬라이드를 불러오는 함수
function image_slide_view(){
    nav_hidden.classList.add('hidden');
    footer_hidden.classList.add('hidden');
    body_style.style.paddingTop = '0px'
    body_style.classList.add('body_background')
    main_content.classList.add('main_background')
    const image = `
            <i id="slide_delete" class="fas fa-times"></i>
            <div class="swiper-container">
                <div id="item_slide_form2_1" class="swiper-wrapper">
                    <div class="item_slide_form2_2 swiper-slide">
                        <img class="item_slide_big_picture" src="/imgs/slideshow_sample.jpg">
                    </div>
                    <div class="item_slide_form2_2 swiper-slide">
                        <img class="item_slide_big_picture" src="/imgs/food1.jpg">
                    </div>
                </div>
                <div class="swiper-pagination"></div>
                <div class="swiper-button-prev"></div>
                <div class="swiper-button-next"></div>
            </div>`
    main_content.innerHTML = image
    const mySwiper = new Swiper('.swiper-container', {
        // autoHeight: true,
        // slidesPerView: 1,
        loop: true,
        pagination: {
            el: '.swiper-pagination',
            clickable: true,
        },
        navigation: {
            nextEl: '.swiper-button-next',
            prevEl: '.swiper-button-prev',
        },
    })
}
// 이미지 슬라이드를 닫는 함수
function image_slide_close(){
    nav_hidden.classList.remove('hidden');
    footer_hidden.classList.remove('hidden');
    body_style.style.paddingTop = (nav_hidden.clientHeight+3)+'px'
    body_style.classList.remove('body_background')
    main_content.classList.remove('main_background')
}

document.addEventListener('click',function(event) {
    if(hasClass(event.target, 'item_slide_picture')) {
        image_slide_view()
    }
    if (event.target.id == "slide_delete"){
        image_slide_close()
        $('main').load('/shop/item/onePick'+' main>',function () {
            window.scrollTo(0, 0);
        });
    }
})

window.addEventListener('popstate',function () {
    if(document.referrer.indexOf('/shop/item/onePick') > -1){
        image_slide_close()
    }
})