package org.jetbrains.android.database;

import com.intellij.database.dialects.DatabaseDialectEx;
import com.intellij.database.dialects.SqliteDialect;
import com.intellij.database.psi.BasicDataSourceManager;
import com.intellij.database.util.DbSqlUtil;
import com.intellij.facet.ProjectFacetManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.sql.dialects.SqlLanguageDialect;
import com.intellij.util.Consumer;
import icons.AndroidIcons;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Eugene.Kudelevsky
 */
public class AndroidDbManager extends BasicDataSourceManager<AndroidDataSource> {
  public static final String NOTIFICATION_GROUP_ID = "Android Data Source Manager";

  private final AndroidDataSourceStorage myStorage;

  public AndroidDbManager(@NotNull Project project, @NotNull AndroidDataSourceStorage storage) {
    super(project, storage.getDataSources());
    myStorage = storage;
  }

  @Nullable
  @Override
  public DatabaseDialectEx getDatabaseDialect(@NotNull AndroidDataSource element) {
    return SqliteDialect.INSTANCE;
  }

  @Nullable
  @Override
  public SqlLanguageDialect getSqlDialect(@NotNull AndroidDataSource element) {
    return DbSqlUtil.findSqlDialect(SqliteDialect.INSTANCE);
  }

  @Override
  public void renameDataSource(@NotNull AndroidDataSource element, @NotNull String name) {
    element.setName(name);
    updateDataSource(element);
  }

  @Override
  public void addDataSource(@NotNull AndroidDataSource dataSource) {
    myStorage.addDataSource(dataSource);
    attachDataSource(dataSource);
  }

  @Override
  public void removeDataSource(@NotNull AndroidDataSource element) {
    myStorage.removeDataSource(element);
    detachDataSource(element);
  }

  @NotNull
  @Override
  public Configurable createDataSourceEditor(@NotNull AndroidDataSource element) {
    return new AndroidDataSourceConfigurable(this, myProject, element);
  }

  @Override
  public AnAction getCreateDataSourceAction(@NotNull Consumer<AndroidDataSource> consumer) {
    if (!ProjectFacetManager.getInstance(myProject).hasFacets(AndroidFacet.ID)) return null;
    return new DumbAwareAction("Android SQLite", null, AndroidIcons.Android) {
      @Override
      public void actionPerformed(AnActionEvent e) {
        AndroidDataSource result = new AndroidDataSource();
        result.setName(getTemplatePresentation().getText());
        result.resolveDriver();
        consumer.consume(result);
      }
    };
  }

  @NotNull
  @Override
  public AndroidDataSource copyDataSource(@NotNull String newName, @NotNull AndroidDataSource copyFrom) {
    AndroidDataSource result = copyFrom.copy();
    result.setName(newName);
    result.resolveDriver();
    return result;
  }

}
