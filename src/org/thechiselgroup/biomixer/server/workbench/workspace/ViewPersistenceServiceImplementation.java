/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 *******************************************************************************/
package org.thechiselgroup.biomixer.server.workbench.workspace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import org.thechiselgroup.biomixer.client.workbench.authentication.AuthenticationException;
import org.thechiselgroup.biomixer.client.workbench.authentication.AuthorizationException;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.ViewDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.ViewPreviewDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.service.ViewPersistenceService;
import org.thechiselgroup.choosel.core.client.util.ServiceException;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.inject.Inject;

/**
 * Design rationale: everything that will not be used for querying is just
 * serialized and not mapped to persistable objects (reason: better performance
 * & better development performance)
 * 
 * For more information on App Engine persistence, see
 * 
 * {@linkplain http://code.google.com/appengine/docs/java/datastore/}
 * 
 * {@linkplain http
 * ://code.google.com/events/io/2009/sessions/SofterSideofSchemas.html}
 */
public class ViewPersistenceServiceImplementation implements
        ViewPersistenceService {

    private final PersistenceManagerFactory persistenceManagerFactory;

    private final UserService userService;

    @Inject
    public ViewPersistenceServiceImplementation(PersistenceManagerFactory pmf,
            UserService userService) {
        assert userService != null;
        assert pmf != null;

        this.userService = userService;
        persistenceManagerFactory = pmf;
    }

    public void checkAuthenticated() throws AuthenticationException {
        if (!userService.isUserLoggedIn()) {
            throw new AuthenticationException(
                    "Authentication failed: User not signed in.");
        }
    }

    private PersistenceManager createPersistanceManager() {
        return persistenceManagerFactory.getPersistenceManager();
    }

    private PersistentView createPersistentView(PersistenceManager pm) {
        PersistentView view = new PersistentView();
        view = pm.makePersistent(view);

        return view;
    }

    private void deletePersistentView(PersistentView pView,
            PersistenceManager manager) {
        manager.deletePersistent(pView);
    }

    @Override
    public Long deleteView(Long id) throws ServiceException {
        PersistenceManager manager = createPersistanceManager();
        try {
            User user = getCurrentUser();

            return deleteView(id, manager, user);
        } finally {
            manager.close();
        }
    }

    private Long deleteView(Long id, PersistenceManager manager, User user)
            throws ServiceException {
        PersistentView persistentView = getPersistentView(id, manager);
        if (!persistentView.getUserId().equals(user.getUserId())) {
            throw new AuthenticationException(
                    "Authentication failed: Tsk Tsk, no trying to delete others views.  UID should have been "
                            + persistentView.getUserId()
                            + ", but was "
                            + user.getUserId());
        }

        deletePersistentView(persistentView, manager);

        return null;

    }

    private User getCurrentUser() throws AuthenticationException {
        checkAuthenticated();
        return userService.getCurrentUser();
    }

    private PersistentView getPersistentView(Long viewId,
            PersistenceManager manager) throws AuthorizationException {

        PersistentView pView = manager.getObjectById(PersistentView.class,
                viewId);

        return pView;
    }

    private Collection<PersistentView> getPersistentViewsForUser(
            PersistenceManager manager, User user) {

        Query userIdQuery = manager.newQuery(PersistentView.class,
                "userId == userIdParam");
        userIdQuery.declareParameters("String userIdParam");
        //
        return (Collection<PersistentView>) userIdQuery.execute(user
                .getUserId());
    }

    @Override
    public ViewDTO loadView(Long viewId) throws ServiceException {
        PersistenceManager pm = createPersistanceManager();

        try {
            return loadView(viewId, pm);
        } finally {
            pm.close();
        }
    }

    private ViewDTO loadView(Long viewId, PersistenceManager pm)
            throws AuthorizationException {
        return toViewDTO(getPersistentView(viewId, pm));
    }

    @Override
    public List<ViewPreviewDTO> loadViewPreviews()
            throws AuthenticationException {

        PersistenceManager manager = createPersistanceManager();
        try {
            User user = getCurrentUser();

            return loadViewPreviews(manager, user);
        } finally {
            manager.close();
        }
    }

    private List<ViewPreviewDTO> loadViewPreviews(PersistenceManager manager,
            User user) {
        Collection<PersistentView> views = getPersistentViewsForUser(manager,
                user);
        List<ViewPreviewDTO> result = new ArrayList<ViewPreviewDTO>();
        for (PersistentView view : views) {
            result.add(toViewPreviewDTO(view));
        }
        return result;
    }

    @Override
    public Long saveView(ViewDTO dto) throws AuthenticationException,
            AuthorizationException {

        PersistenceManager pm = createPersistanceManager();

        try {
            User user = getCurrentUser();
            PersistentView view = createPersistentView(pm);

            updateViewWithDTO(view, dto, user);

            return view.getId();
        } finally {
            pm.close();
        }
    }

    private ViewDTO toViewDTO(PersistentView pView) {
        ViewDTO dto = new ViewDTO();

        dto.setId(pView.getId());
        dto.setTitle(pView.getTitle());
        dto.setResources(pView.getResources());
        dto.setResourceSets(pView.getResourceSets());
        dto.setContentType(pView.getContentType());
        dto.setTitle(pView.getTitle());
        dto.setViewState(pView.getViewState());

        return dto;
    }

    private ViewPreviewDTO toViewPreviewDTO(PersistentView view) {
        return new ViewPreviewDTO(view.getId(), view.getTitle(),
                view.getContentType(), view.getSharedDate());
    }

    private void updateViewWithDTO(PersistentView view, ViewDTO dto, User user) {
        view.setTitle(dto.getTitle());
        view.setViewState(dto.getViewState());
        view.setResources(dto.getResources());
        view.setResourceSets(dto.getResourceSets());
        view.setContentType(dto.getContentType());

        view.setUserName(user.getNickname());
        view.setUserId(user.getUserId());
        view.setUserEmail(user.getEmail());
        view.setSharedDate(new Date());
    }
}
