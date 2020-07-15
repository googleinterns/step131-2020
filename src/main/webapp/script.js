$(document).ready(function() {
    fetch('/query-cloud').then((response) => response.json()).then((array) => {
        // array '{}' on page load: request made before form submission
        // array not '{}' when request is made after form submission.
        if (array !== '{}') {
            for (let i = 0; i < array.length; i++) {
                const url = array[i].url;
                // TODO: create entire image list structure
                $('#requested-images').append(`<li><img src='${url}'></li>`);
            }
        }
    });

    // This fetch loads the location options for the form through Datastore.
    fetch('/form-locations').then((response) => response.json())
    .then((locations) => {
        $('#locations').empty();
        const emptyOption = $('<option></option>').attr('value', '').text('');
        $('#locations').append(emptyOption);
        for (let j = 0; j < locations.length; j++) {
            const option = $('<option></option>').attr('value', locations[j])
            .text(locations[j]);
            $('#locations').append(option);
        }
    });
})

// CODE BELOW not incorporated currently. Referenced for jQuery above.


// /** Removes any current description and image li elements on the page. */
// async function clearImages() {
//     var list = document.getElementById('requested-images');
//     // As long as <ul> has a child node, remove it
//     while (list.hasChildNodes()) {
//         list.removeChild(list.firstChild);
//     }
// }

// /** Builds Unordered List of map snapshots and their descriptions. */
// function getImages() {
//     fetch('/query-drive').then(response => response.json())
//     .then((listMapImages) => {
//         // listMapImages is MapImages with a url image attribute.
//         const imageDisplay = document.getElementById('requested-images');
//         listMapImages.forEach((mapImage) => {
//             imageDisplay.appendChild(createListDescrip(mapImage));
//             imageDisplay.appendChild(createListImage(mapImage.getURL()));
//         });
//     });
// }

// /** Creates a description list element. */
// function createListDescrip(image) {
//     const liElement = document.createElement('li');
//     description = image.cityName + ' on ' + image.month + ', ' + image.year 
//         + ' ' + image.timeStamp + ' at zoom ' + image.zoomAd;
//     liElement.innerText = description;
//     return liElement;
// }

// /** Creates an <img> list element. */
// function createListImage(url) {
//     const liElement = document.createElement('li');
//     var imgElement = document.createElement('img');
//     imgElement.setAttribute('src', url);
//     liElement.appendChild(imgElement)
//     return liElement;
// }
