/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.server.issue.filter;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.core.issue.db.IssueFilterDto;
import org.sonar.server.tester.UserSessionRule;
import org.sonar.server.ws.WsTester;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchActionTest {
  @Rule
  public UserSessionRule userSessionRule = UserSessionRule.standalone();

  @Mock
  IssueFilterService service;

  IssueFilterWriter writer = new IssueFilterWriter();

  SearchAction action;

  WsTester tester;

  @Before
  public void setUp() {
    action = new SearchAction(service, writer, userSessionRule);
    tester = new WsTester(new IssueFilterWs(mock(AppAction.class), mock(ShowAction.class), action, mock(FavoritesAction.class)));
  }

  @Test
  public void anonymous_app() throws Exception {
    userSessionRule.anonymous();
    tester.newGetRequest("api/issue_filters", "search").execute().assertJson(getClass(), "anonymous_page.json");
  }

  @Test
  public void logged_in_app() throws Exception {
    userSessionRule.login("eric").setUserId(123);
    tester.newGetRequest("api/issue_filters", "search").execute()
      .assertJson(getClass(), "logged_in_page.json");
  }

  @Test
  public void logged_in_app_with_favorites() throws Exception {
    userSessionRule.login("eric").setUserId(123);
    when(service.findFavoriteFilters(userSessionRule)).thenReturn(Arrays.asList(
      new IssueFilterDto()
        .setId(3L)
        .setName("My Unresolved Issues")
        .setShared(true)
        .setData("resolved=false|assignees=__me__"),
      new IssueFilterDto()
        .setId(2L)
        .setName("False Positive and Won't Fix Issues")
        .setShared(false)
        .setData("resolutions=FALSE-POSITIVE,WONTFIX")
    ));
    tester.newGetRequest("api/issue_filters", "search").execute()
      .assertJson(getClass(), "logged_in_page_with_favorites.json");
  }
}
