$(document).ready(function() {
    fetch("/query-cloud").then(response => response.json()).then(array => {
        // array will be "{}" since a request is made before the form is submitted (on page load).
        // array will not equal "{}" when request is made after form submission.
        if (array !== "{}") {
            for (var i = 0; i < array.length; i++) {
                const url = array[i].url;
                // TODO: create entire image list structure
                $("#requestedImages").append(`<li><img src="${url}"></li>`);
            }
        }
    });

    // This fetch loads the location options for the form through Datastore.
    fetch("/form-locations").then(response => response.json()).then(locations => {
        $("#locations").empty();
        var emptyOption = $('<option></option>').attr("value", "").text("");
        $("#locations").append(emptyOption);
        for (var j = 0; j < locations.length; j++) {
            var option = $('<option></option>').attr("value", locations[j]).text(locations[j]);
            $("#locations").append(option);
        }
    });
    
    loadDateRange();
})

/** Removes any current description and image li elements on the page. */
async function clearImages() {    
    var list = document.getElementById("requestedImages");
    // As long as <ul> has a child node, remove it
    while (list.hasChildNodes()) {
        list.removeChild(list.firstChild);
    }
}

// CODE BELOW is not incorporated currently, but is being referenced while writing jQuery above.

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

function loadDateRange() {
    let startDate = moment().subtract(29, 'days');
    let endDate = moment();

    function callback(start, end) {
        $('#request-form').submit((eventObj) => {
            $('<input />').attr("type", "hidden")
                .attr("name", "startDateRange")
                .attr("value", start.valueOf())
                .appendTo('#form');
            $('<input />').attr("type", "hidden")
                .attr("name", "endDateRange")
                .attr("value", end.valueOf())
                .appendTo('#form');
                //TODO: the JQuery.value below is undefined.
            console.log("startDate: " + $('input[name="startDateRange"]').value);
            console.log("endDate: " + $('input[name="endDateRange"]').value);
            return true;
        });
    }

    $('input[name="dateFilter"]').daterangepicker({
        "showDropdowns": true,
         ranges: {
            'A month ago': [moment().subtract(1, 'month'), moment()],
            '3 months ago': [moment().subtract(3, 'month'), moment()],
            '6 months ago': [moment().subtract(6, 'month'), moment()],
            'A year ago': [moment().subtract(1, 'year'), moment()],
            '18 months ago': [moment().subtract(18, 'month'), moment()],
            '2 years ago': [moment().subtract(2, 'year'), moment()]
        },
        "linkedCalendars": false,
        "alwaysShowCalendars": true,
        "startDate": startDate,
        "endDate": endDate,
        "minDate": "07/01/2020"
    }, callback);

    //callback(start, end);
}