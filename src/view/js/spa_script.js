var links = null; 
var form_inputs = null;
var loaded = true; 
var data = 
{

    table: "",
    link: ""
};

var page = //Элементы, текст в которых будет меняться
{
    table: document.getElementById("table"),
    forms_div: document.getElementById("forms_div")
    
};

OnLoad();


function OnLoad()
{
    var link = window.location.pathname; //Ссылка страницы без домена

    LinkClick(link);
}

function InitLinks()
{
    links = document.getElementsByClassName("link_internal"); //Находим все ссылки на странице
    form_inputs = document.getElementsByClassName("form_internal_input");

    for (var i = 0; i < links.length; i++)
    {
   	 //Отключаем событие по умолчанию и вызываем функцию LinkClick
   	 links[i].addEventListener("click", function (e)
   	 {
   		 e.preventDefault();
   		 LinkClick(e.target.getAttribute("href"));
   		 return false;
   	 });
    }
    for (var i = 0; i < form_inputs.length; i++){
    	 form_inputs[i].addEventListener("click", function (e)
       	 {
       		 e.preventDefault();
       		 FormInputClick(e.target.getAttribute("value"),e.target.getAttribute("id"));
       		 return true;
       	 });
        }
}


function LinkClick(href)
{
    switch(href){
        case 'edit.html':
            SendRequest("",href);
            break;
        default:
             SendRequest("?",href);
             break;
    }
}

function FormInputClick(value,id){

    if (value === "updateTable") {
        SendRequest("?","/index.html");
    } else if (value === "put"){
         sendPutRequest(id ,"/index.html");
    }
}

function parseDataFromForm(input_id){
    let form_input_element = document.getElementById(input_id);
    let input_td = form_input_element.parentNode
    let input_tr = input_td.parentElement;
    let cellsCount = input_tr.children.length;

    var dataWillReturn = "";

    console.log("first length = " + input_tr.children.length);    
    console.log("length = " + cellsCount);
    let i = 0;
    while(input_td.previousElementSibling !== null ){
        let name = input_td.previousElementSibling.childNodes[0].getAttribute("name");
        let value = input_td.previousElementSibling.childNodes[0].value;
        console.log("name child" + i + " = " + name);
        console.log("value child" + i + " = " + value);
        input_td = input_td.previousElementSibling;

        dataWillReturn += name;
        dataWillReturn += "=";
        dataWillReturn += value;
        dataWillReturn += "&";
        
        i += 1;
    }
    return dataWillReturn;
}

function sendPutRequest(id,link){
    var xhr = new XMLHttpRequest();

    xhr.open('PUT', 'index.html', true);

    xhr.onreadystatechange = function() //Указываем, что делать, когда будет получен ответ от сервера
    {
   	 if (xhr.readyState != 4) return; //Если это не тот ответ, который нам нужен, ничего не делаем

   	 //Иначе говорим, что сайт загрузился
   	 loaded = true;

   	 if (xhr.status == 200) //Если ошибок нет, то получаем данные
   	 {
   	  GetData((xhr.responseText), link);
   	 }
   	 else //Иначе выводим сообщение об ошибке
   	 {
   		 alert("Loading error! Try again later.");
   		 console.log(xhr.status + ": " + xhr.statusText);
   	 }
    }

    loaded = false; //Говорим, что идёт загрузка

    //Устанавливаем таймер, который покажет сообщение о загрузке, если она не завершится через 2 секунды
    setTimeout(ShowLoading, 2000);
    xhr.send(parseDataFromForm(id)); //Отправляем запрос
}

function SendRequest(query, link)
{
    var xhr = new XMLHttpRequest(); //Создаём объект для отправки запроса

    xhr.open("GET", link + query, true); //Открываем соединение

    xhr.onreadystatechange = function() //Указываем, что делать, когда будет получен ответ от сервера
    {
   	 if (xhr.readyState != 4) return; //Если это не тот ответ, который нам нужен, ничего не делаем

   	 //Иначе говорим, что сайт загрузился
        loaded = true;
        if( link === window.location.pathname){
   	        if (xhr.status == 200) //Если ошибок нет, то получаем данные
   	        {
   	            GetData((xhr.responseText), link);
   	        }
   	        else //Иначе выводим сообщение об ошибке
   	        {
   		        alert("Loading error! Try again later.");
   		        console.log(xhr.status + ": " + xhr.statusText);
            }
        } else {
            HTML = document.querySelector("html");
            HTML.innerHTML = "";
            HTML.innerHTML = xhr.responseText;
           
            window.history.pushState(xhr.responseText,null,link);
        }
    }

    loaded = false; //Говорим, что идёт загрузка

    //Устанавливаем таймер, который покажет сообщение о загрузке, если она не завершится через 2 секунды
    setTimeout(ShowLoading, 2000);
    xhr.send(); //Отправляем запрос
}

window.onpopstate = function(event) {
    HTML = document.querySelector("html");
    HTML.innerHTML = "";
    HTML.innerHTML = (event.state);
  }



function GetData(response, link) //Получаем данные
{
    data =
    {
   	 table: response
    };

    UpdatePage(); //Обновляем контент на странице
}

function ShowLoading()
{
    if(!loaded) //Если страница ещё не загрузилась, то выводим сообщение о загрузке
    {
   	 page.body.innerHTML = "Loading...";
    }
}

function UpdatePage() //Обновление контента
{
    let jsonData = JSON.parse(data.table);
    let tr_head = document.createElement("tr");
    let tabSymbols = "\t\t\t\t\t";
    page.forms_div.innerHTML = "";
    for (var i = 0; i < jsonData[0].length; i++) {
          let th = document.createElement("th");
          th.appendChild(document.createTextNode(jsonData[0][i]));

          tr_head.appendChild(th);
          tr_head.appendChild(document.createTextNode("\n"));
          tr_head.appendChild(document.createTextNode(tabSymbols));

          let updateForm = document.createElement("form");
          updateForm.setAttribute("method","post");;
          updateForm.setAttribute("id","updForm" + i);
          page.forms_div.appendChild(updateForm);
    }
    page.table.innerHTML = "";
    page.table.appendChild(tr_head);

     for (var row = 1; row < jsonData.length; row++) {
        let tr = document.createElement("tr");

         for (var col = 0; col < jsonData[row].length; col++) {
                
            let td = document.createElement("td");
            let inputElement = document.createElement("input");

            inputElement.setAttribute("name",jsonData[0][col]);
            inputElement.setAttribute("value",jsonData[row][col]);
            inputElement.setAttribute("size","8");
            inputElement.setAttribute("form","updForm" + row);
            inputElement.setAttribute("class","form_internal_input")

            if(jsonData[0][col].toString(10).includes("_ID")){
                inputElement.setAttribute("readonly","readonly");
            }

            td.appendChild(inputElement);

            tr.appendChild(td);
            tr.appendChild(document.createTextNode("\n"));
            tr.appendChild(document.createTextNode(tabSymbols));
         }

         let td_update = document.createElement("td");
         let inputUpdate = document.createElement("input");
         let inputDelete = document.createElement("input");

         inputUpdate.setAttribute("type","submit");
         inputUpdate.setAttribute("form","updForm" + row);
         inputUpdate.setAttribute("name", "_isMethod");
         inputUpdate.setAttribute("value","put");
         inputUpdate.setAttribute("class","form_internal_input")
         inputUpdate.setAttribute("id","put" + row);

         inputDelete.setAttribute("type","submit");
         inputDelete.setAttribute("form","updForm" + row);
         inputDelete.setAttribute("name", "_isMethod");
         inputDelete.setAttribute("value","delete");
         inputDelete.setAttribute("class","form_internal_input")
         inputDelete.setAttribute("id","delete" + row);

         td_update.appendChild(inputUpdate);
         td_update.appendChild(inputDelete);

         tr.appendChild(td_update);
         page.table.appendChild(tr);

    } 
    pageState = document.querySelector("html").outerHTML;

    window.history.pushState( pageState, null, data.link); //Меняем ссылку

    InitLinks(); //Инициализируем новые ссылки
}
