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
                const div = document.createElement('div');
                div.className = 'tile';
                div.id = 'Item' + i;

                const atag = document.createElement('a');
                atag.id = 'a' + i;
                $('a' + i).attr('href', '#');

                const image = document.createElement('img');
                image.src = array[i].url;
                const fileName = array[i].objectID.replace(/\//g, '_');
                image.setAttribute('filename', fileName);

                h4 = document.createElement('h4');
                h4.textContent = array[i].cityName + ' on ' + array[i].month +
                    '/' + array[i].year + ' at Zoom Level ' + array[i].zoom;

                atag.appendChild(image);
                atag.appendChild(h4);
                div.appendChild(atag);
                $('#requested-images').append(div);
            }
            if (array.length > 0) {
                $('#download-btn').removeClass('disabled');
            }
            else {
                $('#download-btn').addClass('disabled');
            }
        });
    });

    $('#requested-images').on('click', '.tile a', function(event) {
        const gridImage = $(this).children('img');
        const url = gridImage.attr('src');
        $('#overlay img').attr('src', url);
        $('#overlay').show();
    });

    $("#overlay").click(function(event) {
        if (event.target.id !== "overlay-img") {
            $(this).hide();
        }
    });

    // Download the displayed images
    $('#download-btn').click(function(event) {
        if ($(this).hasClass('disabled')) return;
        event.preventDefault();
        const zip = new JSZip();
        const images = Array.from(document.querySelectorAll('#requested-images .tile a img'));
        const imagePromises = images.map(function callback(currentImage) {
            const fileName = currentImage.getAttribute('filename');
            const url = currentImage.getAttribute('src');
            return urlToPromise(url).then(function(blob) {
                zip.file(fileName, blob, {binary: true});
            });
        });

        Promise.all(imagePromises).then(function() {
            const date = new Date(Date.now());
            const dateString = date.toLocaleDateString().replace(/\//g, '-');
            const timeArray = date.toLocaleTimeString().split(' ')[0].split(':');
            const hrs = timeArray[0];
            const mins = timeArray[1];
            zip.generateAsync({type: 'blob'})
                .then(function (blob) {
                    saveAs(blob, `Map Snapshot Images from ${dateString}_${hrs}_${mins}.zip`);
                });
        });
    });

    // Upload files any files to Drive that need to be uploaded
    fetch('/start-save-drive');
});

function urlToPromise(url) {
    return new Promise(function(resolve, reject) {
        JSZipUtils.getBinaryContent(url, function (err, data) {
            if (err) {
                reject(err);
            } else {
                resolve(data);
            }
        });
    });
}

window.map = undefined;

/** Makes a map and adds it to the page. */
function createMap() {
    window.map = new google.maps.Map(document.getElementById('map'),
        {center: {lat: 35.9128, lng: -100.3821}, zoom: 5});
}

const locOrderSelected = new Set();
/** Update preview location to most recently selected location in form. */
function updateLocation() {
    let location;
    const selectedCoords = [];
    const locations = document.getElementById('locations');
    for (let i = 0; i < locations.length; i++) {
        location = locations.options[i];
        if (location.selected) {
            selectedCoords.push(location.getAttribute('coords'));
        }
    }

    if (selectedCoords.length == 0) {
        (window.map).panTo(new google.maps.LatLng(35.9128, -100.3821));
    } else {
        for (const location of selectedCoords) {
            if (!(locOrderSelected.has(location))) {
                locOrderSelected.add(location);
            }
        }

        const selectedCoordsSET = new Set(selectedCoords);
        for (const loc of Array.from(locOrderSelected.values())) {
            if (!((selectedCoordsSET).has(loc))) {
                locOrderSelected.delete(loc);
            }
        }

        const locOrderARRAY = Array.from(locOrderSelected.values());
        const lastLocationSelected = locOrderARRAY[locOrderARRAY.length - 1];
        const coords = lastLocationSelected.split(' ');
        const center = new google.maps.LatLng(parseFloat(coords[0]),
            parseFloat(coords[1]));
        (window.map).panTo(center);
    }
}

const zoomOrderSelected = new Set();
/** Update preview zoom to most recently form-selected zoom level. */
function updateZoom() {
    const selectedZooms = $('#zoom').val();
    if (selectedZooms.length == 0) {
        (window.map).setZoom(5);
    } else {
        for (const zoom1 of selectedZooms) {
            if (!(zoomOrderSelected.has(zoom1))) {
                zoomOrderSelected.add(zoom1);
            }
        }

        const selectedZoomsSET = new Set(selectedZooms);
        for (const zoom2 of Array.from(zoomOrderSelected.values())) {
            if (!(selectedZoomsSET.has(zoom2))) {
                zoomOrderSelected.delete(zoom2);
            }
        }

        const zoomOrderARRAY = Array.from(zoomOrderSelected.values());
        const lastZoomSelected = zoomOrderARRAY[zoomOrderARRAY.length - 1];
        (window.map).setZoom(parseInt(lastZoomSelected));
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
