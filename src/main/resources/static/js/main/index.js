// 카테고리 버튼 클릭하면 해당 카테고리 위치로 스크롤 이동
function scroll_category_item(event){
    const navbar = document.getElementById('navbar_height');
    const category_title = document.getElementsByClassName('category_title');
    const index_bg = document.getElementsByClassName('index_bg')
    for (let i = 0; i < index_bg.length; i++) {
        if(event.target == index_bg[i] ){
            window.scrollTo({top:category_title[i].offsetTop - navbar.offsetHeight, behavior:'smooth'});
        }
    }
}
document.addEventListener('click',scroll_category_item)

