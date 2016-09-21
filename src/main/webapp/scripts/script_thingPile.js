

function test1(selector,words){
    document.querySelector(selector+"").textContent = words;
    return;
}

function alertTest(){
  alert("ClicketyClickety");
}

function Thing(id, name, attributes, extra){
  this.id = id;
  this.name = name;
  this.attributes = new Map();
  this.extra = extra;

}

function Attribute(id, name, type, value){
    this.id = id;
    this.name = name;
    this.type = type;
    this.value = value;
}


function newThingDom(){
  var newDiv1 = document.createElement('div');
  newDiv1.className = 'Thing';
  newDiv1.style.color = "white";
  newDiv1.innerHTML="<h2>ThingName</h2>";


  /*
  newDiv.innerHTML="hi";*/
  document.getElementById('things_content').appendChild(newDiv1);
  return;
}


//function for making a new general attribute appear in the DOM in the
//attributes panel
function newAttributeDomPanel(attributeName){
  var newDivAttr = document.createElement('div');
  newDivAttr.className = 'attribute_container_panel';
  newDivAttr.innerHTML="Attribute:<h3>" + attributeName + "</h3>";


  document.getElementById('attributes_content').appendChild(newDivAttr);
  return;
}


//function for making an attribute appear in the DOM in a given Thing object
// using a given Attribute object
function newAttributeDomThing(thingObj, attributeObj){
  var newDivAttr = document.createElement('div');
  newDivAttr.className = 'attribute_container_thing';
  newDivAttr.innerHTML= attributeObj.name + "<br>Value<button>+</button><button>-</button><br><input type=\"number\" name=\"attrVal\">";

  document.getElementById(thingObj.id).appendChild(newDivAttr);
  return;
}


/*
function getText(){
  var textIn = prompt('enter element');
  localStorage.setItem('input',textIn);
  var textIn2 = prompt('enter text');
  localStorage.setItem('input2',textIn2);
  test1(textIn,textIn2);
  return;
}
*/

document.querySelector("#main_header_sub2").onclick = function(){
  if(document.querySelector("#main_header_sub2").textContent === "KlaymoJones"){
    test1("#main_header_sub2","KlaySquared")
  }
  else{
    test1("#main_header_sub2","KlaymoJones");
  }
};
