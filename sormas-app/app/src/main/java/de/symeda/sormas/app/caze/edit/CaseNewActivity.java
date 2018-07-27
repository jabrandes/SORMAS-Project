package de.symeda.sormas.app.caze.edit;

import android.content.Context;
import android.os.AsyncTask;
import android.view.Menu;

import java.util.Calendar;

import de.symeda.sormas.api.caze.CaseClassification;
import de.symeda.sormas.api.utils.ValidationException;
import de.symeda.sormas.app.BaseActivity;
import de.symeda.sormas.app.BaseEditActivity;
import de.symeda.sormas.app.BaseEditFragment;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.person.Person;
import de.symeda.sormas.app.component.menu.PageMenuItem;
import de.symeda.sormas.app.core.notification.NotificationHelper;
import de.symeda.sormas.app.person.SelectOrCreatePersonDialog;
import de.symeda.sormas.app.core.async.AsyncTaskResult;
import de.symeda.sormas.app.core.async.SavingAsyncTask;
import de.symeda.sormas.app.core.async.TaskResultHolder;
import de.symeda.sormas.app.shared.CaseFormNavigationCapsule;
import de.symeda.sormas.app.util.Consumer;
import de.symeda.sormas.app.component.validation.FragmentValidator;

import static de.symeda.sormas.app.core.notification.NotificationType.ERROR;

public class CaseNewActivity extends BaseEditActivity<Case> {

    public static final String TAG = CaseNewActivity.class.getSimpleName();

    private AsyncTask saveTask;

    @Override
    public CaseClassification getPageStatus() {
        return (CaseClassification) super.getPageStatus();
    }

    @Override
    protected Case queryRootEntity(String recordUuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Case buildRootEntity() {
        Person _person = DatabaseHelper.getPersonDao().build();
        Case _case = DatabaseHelper.getCaseDao().build(_person);
        return _case;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        getSaveMenu().setTitle(R.string.action_save_case);
        return result;
    }

    @Override
    protected BaseEditFragment buildEditFragment(PageMenuItem menuItem, Case activityRootData) {
        CaseFormNavigationCapsule dataCapsule = new CaseFormNavigationCapsule(this, getRootEntityUuid(), getPageStatus());
        return CaseNewFragment.newInstance(dataCapsule, activityRootData);
    }

    @Override
    protected int getActivityTitle() {
        return R.string.heading_case_new;
    }

    @Override
    public void replaceFragment(BaseEditFragment f) {
        super.replaceFragment(f);
        getActiveFragment().setLiveValidationDisabled(true);
    }

    @Override
    public void saveData() {
        final Case caze = getStoredRootEntity();
        CaseNewFragment fragment = (CaseNewFragment) getActiveFragment();

        if (fragment.isLiveValidationDisabled()) {
            fragment.disableLiveValidation(false);
        }

        try {
            FragmentValidator.validate(getContext(), fragment.getContentBinding());
        } catch (ValidationException e) {
            NotificationHelper.showNotification(this, ERROR, e.getMessage());
            return;
        }

        SelectOrCreatePersonDialog.selectOrCreatePerson(caze.getPerson(), new Consumer<Person>() {
            @Override
            public void accept(Person person) {
                caze.setPerson(person);

                saveTask = new SavingAsyncTask(getRootView(), caze) {
                    @Override
                    protected void onPreExecute() {
                        showPreloader();
                    }

                    @Override
                    protected void doInBackground(TaskResultHolder resultHolder) throws Exception {
                        DatabaseHelper.getPersonDao().saveAndSnapshot(caze.getPerson());

                        // epid number
                        Calendar calendar = Calendar.getInstance();
                        String year = String.valueOf(calendar.get(Calendar.YEAR)).substring(2);
                        caze.setEpidNumber(caze.getRegion().getEpidCode() != null ? caze.getRegion().getEpidCode() : ""
                                + "-" + caze.getDistrict().getEpidCode() != null ? caze.getDistrict().getEpidCode() : ""
                                + "-" + year + "-");

                        DatabaseHelper.getCaseDao().saveAndSnapshot(caze);
                    }


                    @Override
                    protected void onPostExecute(AsyncTaskResult<TaskResultHolder> taskResult) {
                        hidePreloader();
                        super.onPostExecute(taskResult);
                        if (taskResult.getResultStatus().isSuccess()) {
                            showCaseEditView(caze);
                        }
                    }
                }.executeOnThreadPool();
            }
        });
    }

    private void showCaseEditView(Case caze) {
        CaseFormNavigationCapsule dataCapsule = new CaseFormNavigationCapsule(getContext(),
                caze.getUuid(), caze.getCaseClassification());
        CaseEditActivity.goToActivity(CaseNewActivity.this, dataCapsule);
    }

    public static <TActivity extends BaseActivity> void goToActivity(Context fromActivity, CaseFormNavigationCapsule dataCapsule) {
        BaseEditActivity.goToActivity(fromActivity, CaseNewActivity.class, dataCapsule);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (saveTask != null && !saveTask.isCancelled())
            saveTask.cancel(true);
    }

}