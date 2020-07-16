package com.google.step;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/********
    This servlet is used to push the StartDrive
    task in the TaskQueue
********/

@WebServlet(
    name = "StartSaveDrive",
    description = "taskqueue: Set up SaveDrive.java to run in the background",
    urlPatterns = "/start-save-drive"
)
public class StartSaveDrive extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");
    private final static Logger LOGGER = Logger.getLogger(StartSaveDrive.class.getName());

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind("DriveMapImage")
            .setLimit(1)
            .build();
        QueryResults<Entity> resultList = datastore.run(query);
        // Only run the task if there are remaining DriveMapImageEntities
        if (resultList.hasNext()) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                String accessToken = (String)session.getAttribute("accessToken");
                Queue queue = QueueFactory.getDefaultQueue();
                TaskOptions options = TaskOptions.Builder.withUrl("/save-drive")
                .method(TaskOptions.Method.POST)
                .param("accessToken", accessToken);
                queue.add(options);
            }
        }
    }
}
