$(document).ready(function() {
    $('#locations + button').children('.filter-option')
        .prepend('<span class="dropdown-title">Locations</span>');
    $('#zoom + button').children('.filter-option')
        .prepend('<span class="dropdown-title">Zoom Level</span>');
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
            clearImages();
            for (let i = 0; i < array.length; i++) {
                const url = array[i].url;
                // TODO: create entire image list structure
                $('#requested-images').append(`<li><img src='${url}'></li>`);
            }
        });
    });

    // Upload files any files to Drive that need to be uploaded
    fetch('/start-save-drive');
});

/** Loads the location options for the form through Datastore. */
function loadLocations() {
    fetch('/form-locations').then((response) => response.json())
        .then((locations) => {
            $('#locations').empty();
            for (let j = 0; j < locations.length; j++) {
                const option = $('<option></option>')
                    .attr('value', locations[j]).text(locations[j]);
                $('#locations').append(option);
            }
            $('#locations').val(locations[0]);
            $('#locations').selectpicker('refresh');
            $('#locations').selectpicker('render');
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

            // Debug Area
            $("<p>")
                .text("Start Date: " + startDate)
                .appendTo("#debug");
            $("<p>")
                .text("Start Date Form: " + $('#startDateId').val())
                .appendTo("#debug");
            $("<p>")
                .text("End Date: " + endDate)
                .appendTo("#debug");
            $("<p>")
                .text("End Date Form: " + $('#endDateId').val())
                .appendTo("#debug");
            return true;
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

//function loadDateRange() {
//    let startDate = moment().subtract(1, 'month');
//    let endDate = moment();
//
//    function callback(start, end) {
//        $('#request-form').submit((eventObj) => {
//            // Add date range as hidden values to form.
//            $('<input />').attr("type", "hidden")
//                .attr("id", "startDateId")
//                .attr("value", start.unix())
//                .attr("name", "startDate")
//                .appendTo('#request-form');
//            $('<input />').attr("type", "hidden")
//                .attr("id", "endDateId")
//                .attr("value", end.unix())
//                .attr("name", "endDate")
//                .appendTo('#request-form');
//            return true;
//        });
//    }
//
//    $('input[name="dateFilter"]').daterangepicker({
//        "showDropdowns": true,
//         ranges: {
//            'A month ago': [moment().subtract(1, 'month'), moment()],
//            '3 months ago': [moment().subtract(3, 'month'), moment()],
//            '6 months ago': [moment().subtract(6, 'month'), moment()],
//            'A year ago': [moment().subtract(1, 'year'), moment()],
//            '18 months ago': [moment().subtract(18, 'month'), moment()],
//            '2 years ago': [moment().subtract(2, 'year'), moment()]
//        },
//        "linkedCalendars": false,
//        "alwaysShowCalendars": true,
//        "startDate": startDate,
//        "endDate": endDate,
//        "minDate": "07/01/2020"
//    }, callback);
//}

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
//    var zip = new JSZip();
//    zip.file('img.png', data, {binary:true});zip.generateAsync({type:'blob'})
//     .then(function (blob) {
//         saveAs(blob, 'hello.zip');
//     });
// });
