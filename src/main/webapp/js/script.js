$(document).ready(function() {
    // Attempted code for titles within selectors
    // var div = document.createElement('div');
    // div.className = 'dropdown-title';
    // div.attr('style','font-size:11px');
    // div.attr('style','text-transform: uppercase');
    // div.text('Zoom Level');
    // $('.filter-option-inner').append(div);
    // $('#zoom').selectpicker('refresh');

    // This fetch loads the location options for the form through Datastore.
    fetch('/form-locations').then((response) => response.json())
        .then((locations) => {
            $('#locations').empty();
            for (let j = 0; j < locations.length; j++) {
                console.log(locations[j]);
                const option = $('<option></option>')
                    .attr('value', locations[j]).text(locations[j]);
                $('#locations').append(option);
            }
            $('#locations').val(locations[0]);
            $('.selectpicker').selectpicker('refresh');
        });

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

    loadDateRange();

    // Upload files any files to Drive that need to be uploaded
    fetch('/start-save-drive');
});

/** Load date capability for form. */
function loadDateRange() {
    const startDate = moment().subtract(1, 'month');
    const endDate = moment();

    /**
    * Helper date function.
    * @param {number} start begining of date range.
    * @param {number} end end of date range.
    */
    function callback(start, end) {
        $('#request-form').submit((eventObj) => {
            // Add date range as hidden values to form.
            $('<input />').attr('type', 'hidden')
                .attr('id', 'startDateId')
                .attr('value', start.unix())
                .attr('name', 'startDate')
                .appendTo('#request-form');
            $('<input />').attr('type', 'hidden')
                .attr('id', 'endDateId')
                .attr('value', end.unix())
                .attr('name', 'endDate')
                .appendTo('#request-form');
            return true;
        });
    }

    $('input[name="dateFilter"]').daterangepicker({
        'showDropdowns': true,
        'ranges': {
            'A month ago': [moment().subtract(1, 'month'), moment()],
            '3 months ago': [moment().subtract(3, 'month'), moment()],
            '6 months ago': [moment().subtract(6, 'month'), moment()],
            'A year ago': [moment().subtract(1, 'year'), moment()],
            '18 months ago': [moment().subtract(18, 'month'), moment()],
            '2 years ago': [moment().subtract(2, 'year'), moment()],
        },
        'linkedCalendars': false,
        'alwaysShowCalendars': true,
        'startDate': startDate,
        'endDate': endDate,
        'minDate': '07/01/2020',
    }, callback);
}

/** TODO: Once the html elements for the images are finalized, this code
*   will be used to download the images in a zip folder
*/
// JSZipUtils.getBinaryContent('[imageURL]', function (err, data) {
//    if(err) {
//       throw err; // or handle the error
//    }
//    var zip = new JSZip();
//    zip.file('img.png', data, {binary:true});zip.generateAsync({type:'blob'})
//     .then(function (blob) {
//         saveAs(blob, 'hello.zip');
//     });
// });

/** CODE BELOW not incorporated currently. Referenced for jQuery above. */


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
