$(document).ready(function() {
    fetch("/query-cloud").then(res => res.json()).then(array => {
        // Array is not equal to "{}" when the request is made after the form is submitted
        if(array !== "{}"){
            for(var i = 0; i < array.length; i++) {
                const url = array[i].url;
                // TODO: create entire image list structure
                $("#requestedImages").append(`<li><img src="${url}"></li>`);
            }
        }
    });
    
});

/** Builds Unordered List of map snapshots and their descriptions. */
function getImages() {
    fetch('/query-drive').then(response => response.json()).then((listMapImages) => {       
        // listMapImages is an ArrayList of MapImages with their metadata which includes a url attribute for image data.
        const imageDisplay = document.getElementById('requestedImages');
        listMapImages.forEach((mapImage) => {
            imageDisplay.appendChild(createListDescrip(mapImage));
            imageDisplay.appendChild(createListImage(mapImage.getURL()));
        });
    });
}
 
/** Creates a description list element. */
function createListDescrip(image) {
    const liElement = document.createElement('li');
    description = image.cityName + " on " + image.month + ", " + image.year + " " + image.timeStamp + " at zoom " + image.zoomAd;
    liElement.innerText = description;
    return liElement;
}

/** Creates an <img> list element. */
function createListImage(url) {
    const liElement = document.createElement('li');
    var imgElement = document.createElement("img");
    imgElement.setAttribute("src", url);
    liElement.appendChild(imgElement)
    return liElement;
}


/** Removes any current description and image li elements on the page. */
async function clearImages() {    
    var list = document.getElementById("requestedImages");
    
    // As long as <ul> has a child node, remove it
    while (list.hasChildNodes()) {
        list.removeChild(list.firstChild);
    }
}
