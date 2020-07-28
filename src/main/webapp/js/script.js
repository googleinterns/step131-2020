$(document).ready(function() {
    loadLocations();
    loadDateRange();

    $('#request-form').submit(function(event) {
        event.preventDefault();
        const formData = $('#request-form').serialize();
        const formHeaders = new Headers();
        formHeaders.append('Content-Type', 'application/x-www-form-urlencoded');
        const request = new Request('/frontend-query-datastore',
            {
                method: 'POST',
                headers: formHeaders,
                body: formData,
            });
        fetch(request).then((response) => response.json()).then((array) => {
            $('#requested-images').empty();
            for (let i = 0; i < array.length; i++) {
                const url = array[i].url;
                const div = document.createElement('div');
                div.className = 'tile';
                div.id = 'Item' + i;

                const atag = document.createElement('a');
                atag.id = 'a' + i;
                $('a' + i).attr('href', '#');

                const image = document.createElement('img');
                image.id = 'Image' + i;
                image.src = url;

                h4 = document.createElement('h4');
                h4.textContent = array[i].cityName + ' on ' + array[i].month +
                    '/' + array[i].year + ' zoom level ' + array[i].zoom;
                atag.appendChild(image);
                div.appendChild(atag);
                div.appendChild(h4);
                $('#requested-images').append(div);
            }
        });
    });

    // Upload files any files to Drive that need to be uploaded
    fetch('/start-save-drive');
});

window.map = undefined;

/** Makes a map and adds it to the page. */
function createMap() {
    window.map = new google.maps.Map(document.getElementById('map'),
        {center: {lat: 35.9128, lng: -100.3821}, zoom: 5});
}

const locOrderSet = new Set();
/** Update preview location to most recently selected location in form. */
function updateLocation() {
    let option;
    const locArray = [];
    const locations = document.getElementById('locations');
    for (let i = 0; i < locations.length; i++) {
        option = locations.options[i];
        if (option.selected) {
            locArray.push(option.getAttribute('coords'));
        }
    }
    if (locArray.length == 0) {
        (window.map).panTo(new google.maps.LatLng(35.9128, -100.3821));
    } else {
        const locSet = new Set(locArray);
        for (const location of locArray) {
            if (!(locOrderSet.has(location))) {
                locOrderSet.add(location);
            }
        }
        for (const loc of Array.from(locOrderSet.values())) {
            if (!(locSet.has(loc))) {
                locOrderSet.delete(loc);
            }
        }
        const locOrderArray = Array.from(locOrderSet.values());
        const lastSelected = locOrderArray[locOrderArray.length - 1];
        const coords = lastSelected.split(' ');
        const center = new google.maps.LatLng(parseFloat(coords[0]),
            parseFloat(coords[1]));
        (window.map).panTo(center);
    }
}

const zoomOrderSet = new Set();
/** Update preview zoom to most recently form-selected zoom level. */
function updateZoom() {
    const zoomArray = $('#zoom').val();
    if (zoomArray.length == 0) {
        (window.map).setZoom(5);
    } else {
        const zoomSet = new Set(zoomArray);
        for (const zoom of zoomArray) {
            if (!(zoomOrderSet.has(zoom))) {
                zoomOrderSet.add(zoom);
            }
        }
        for (const zoom of Array.from(zoomOrderSet.values())) {
            if (!(zoomSet.has(zoom))) {
                zoomOrderSet.delete(zoom);
            }
        }
        const zoomOrderArray = Array.from(zoomOrderSet.values());
        const lastSelected = zoomOrderArray[zoomOrderArray.length - 1];
        (window.map).setZoom(parseInt(lastSelected));
    }
}

/** Loads the location options for the form through Datastore. */
function loadLocations() {
    fetch('/form-locations').then((response) => response.json())
        .then((locations) => {
            $('#locations').empty();
            for (let j = 0; j < locations.length; j++) {
                const option = $('<option></option>')
                    .attr('value', locations[j].cityName)
                    .text(locations[j].cityName);
                option.attr('coords', locations[j].latitude + ' ' +
                    locations[j].longitude);
                $('#locations').append(option);
            }
            $('#locations').val(locations[0].cityName);
            $('#locations').selectpicker('refresh');
            $('#locations').selectpicker('render');
            $('#zoom').selectpicker('refresh');
            $('#zoom').selectpicker('render');
            $('#locations + button').children('.filter-option')
                .prepend('<span class="dropdown-title">Locations</span>');
            $('#zoom + button').children('.filter-option')
                .prepend('<span class="dropdown-title">Zoom Level</span>');
        });
}

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

/** Removes any current description and image li elements on the page. */
async function clearImages() {
    const list = document.getElementById('requested-images');
    // As long as <ul> has a child node, remove it
    while (list.hasChildNodes()) {
        list.removeChild(list.firstChild);
    }
}

/** TODO: Once the html elements for the images are finalized, this code
*   will be used to download the images in a zip folder
*/
// JSZipUtils.getBinaryContent('[imageURL]', function (err, data) {
//    if(err) {
//       throw err; // or handle the error
//    }
//    let zip = new JSZip();
//    zip.file('img.png', data, {binary:true});zip.generateAsync({type:'blob'})
//     .then(function (blob) {
//         saveAs(blob, 'hello.zip');
//     });
// });
