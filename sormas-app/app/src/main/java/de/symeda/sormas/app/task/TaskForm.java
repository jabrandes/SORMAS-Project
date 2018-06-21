package de.symeda.sormas.app.task;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;

import java.util.List;

import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.caze.CaseLogic;
import de.symeda.sormas.api.task.TaskHelper;
import de.symeda.sormas.api.task.TaskStatus;
import de.symeda.sormas.api.task.TaskType;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.ValidationException;
import de.symeda.sormas.app.AbstractSormasActivity;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.SormasApplication;
import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.backend.caze.CaseDao;
import de.symeda.sormas.app.backend.caze.CaseDtoHelper;
import de.symeda.sormas.app.backend.common.AbstractDomainObject;
import de.symeda.sormas.app.backend.common.DaoException;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.config.ConfigProvider;
import de.symeda.sormas.app.backend.contact.Contact;
import de.symeda.sormas.app.backend.contact.ContactDao;
import de.symeda.sormas.app.backend.event.Event;
import de.symeda.sormas.app.backend.event.EventDao;
import de.symeda.sormas.app.backend.task.Task;
import de.symeda.sormas.app.backend.task.TaskDao;
import de.symeda.sormas.app.caze.CaseEditActivity;
import de.symeda.sormas.app.contact.ContactEditActivity;
import de.symeda.sormas.app.databinding.TaskFragmentLayoutBinding;
import de.symeda.sormas.app.event.EventEditActivity;
import de.symeda.sormas.app.util.Callback;
import de.symeda.sormas.app.util.ErrorReportingHelper;
import de.symeda.sormas.app.util.FormTab;

/**
 * Created by Stefan Szczesny on 27.07.2016.
 *
 */
public class TaskForm extends FormTab {

    public static final String KEY_TASK_UUID = "taskUuid";

    private TaskFragmentLayoutBinding binding;

    private Tracker tracker;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.task_fragment_layout, container, false);
        editOrCreateUserRight = (UserRight) getArguments().get(EDIT_OR_CREATE_USER_RIGHT);

        SormasApplication application = (SormasApplication) getContext().getApplicationContext();
        tracker = application.getDefaultTracker();

        final String taskUuid = (String) getArguments().getString(Task.UUID);
        final TaskDao taskDao = DatabaseHelper.getTaskDao();
        final Task task = taskDao.queryUuid(taskUuid);

        binding.setTask(task);
        super.onResume();

        if(binding.getTask().getCaze() == null) {
            binding.taskCaze.setVisibility(View.GONE);
        }
        if(binding.getTask().getContact() == null) {
            binding.taskContact.setVisibility(View.GONE);
        }
        if(binding.getTask().getEvent() == null) {
            binding.taskEvent.setVisibility(View.GONE);
        }

        if(binding.getTask().getCreatorUser() == null) {
            binding.taskCreatorUser.setVisibility(View.GONE);
        }

        if (task.getTaskStatus() == TaskStatus.PENDING) {
            binding.taskNotExecutableBtn.setVisibility(View.VISIBLE);
            binding.taskNotExecutableBtn.setText(TaskStatus.NOT_EXECUTABLE.toString());
            binding.taskNotExecutableBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!binding.taskAssigneeReply.getValue().isEmpty()) {
                        try {
                            taskDao.saveAndSnapshot(binding.getTask());
                            taskDao.changeTaskStatus(task, TaskStatus.NOT_EXECUTABLE);
                            ((AbstractSormasActivity)getActivity()).synchronizeChangedData(new Callback() {
                                @Override
                                public void call() {
                                    getActivity().finish();
                                }
                            });
                        } catch (DaoException e) {
                            Log.e(getClass().getName(), "Error while trying to update task status", e);
                            Snackbar.make(getActivity().findViewById(R.id.base_layout), R.string.snackbar_task_status, Snackbar.LENGTH_LONG).show();
                            ErrorReportingHelper.sendCaughtException(tracker, e, task, true);
                        } catch (ValidationException e) {
                            // Will not happen here
                        }
                    } else {
                        Snackbar.make(getActivity().findViewById(R.id.base_layout), R.string.snackbar_task_reply, Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
        else {
            binding.taskNotExecutableBtn.setVisibility(View.GONE);
        }

        if (task.getTaskStatus() == TaskStatus.PENDING) {
            binding.taskDoneBtn.setVisibility(View.VISIBLE);
            binding.taskDoneBtn.setText(TaskStatus.DONE.toString());
            binding.taskDoneBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        taskDao.saveAndSnapshot(binding.getTask());
                        taskDao.changeTaskStatus(task, TaskStatus.DONE);
                        ((AbstractSormasActivity)getActivity()).synchronizeChangedData(new Callback() {
                            @Override
                            public void call() {
                                getActivity().finish();
                            }
                        });
                    } catch (DaoException e) {
                        Log.e(getClass().getName(), "Error while trying to update task status", e);
                        Snackbar.make(getActivity().findViewById(R.id.base_layout), R.string.snackbar_task_status, Snackbar.LENGTH_LONG).show();
                        ErrorReportingHelper.sendCaughtException(tracker, e, task, true);
                    } catch (ValidationException e) {
                        final Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.base_layout), getResources().getString(R.string.snackbar_task_case_classification), Snackbar.LENGTH_INDEFINITE);
                        ((TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text)).setMaxLines(3);
                        snackbar.setAction(R.string.snackbar_okay, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();
                            }
                        });
                        snackbar.show();
                    }
                }
            });
        }
        else {
            binding.taskDoneBtn.setVisibility(View.GONE);
        }

        if (!binding.getTask().getAssigneeUser().equals(ConfigProvider.getUser())) {
            binding.taskAssigneeReply.setVisibility(View.GONE);
            binding.taskDoneBtn.setVisibility(View.GONE);
            binding.taskNotExecutableBtn.setVisibility(View.GONE);
        }

        if (binding.getTask().getCreatorComment() == null || binding.getTask().getCreatorComment().isEmpty()) {
            binding.taskCreatorComment.setVisibility(View.GONE);
        }

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.taskCaze.makeLink(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.getTask().getCaze() != null) {
                    final CaseDao caseDao = DatabaseHelper.getCaseDao();
                    final Case caze = caseDao.queryUuidReference(binding.getTask().getCaze().getUuid());
                    showCaseEditView(caze);
                }
            }
        });
        binding.taskContact.makeLink(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.getTask().getContact() != null) {
                    final ContactDao contactDao = DatabaseHelper.getContactDao();
                    final Contact contact = contactDao.queryUuid(binding.getTask().getContact().getUuid());
                    showContactEditView(contact);
                }
            }
        });
        binding.taskEvent.makeLink(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.getTask().getEvent() != null) {
                    final EventDao eventDao = DatabaseHelper.getEventDao();
                    final Event event = eventDao.queryUuid(binding.getTask().getEvent().getUuid());
                    showEventEditView(event);
                }
            }
        });
    }

    public void showCaseEditView(Case caze) {
        Intent intent = new Intent(getActivity(), CaseEditActivity.class);
        intent.putExtra(CaseEditActivity.KEY_CASE_UUID, caze.getUuid());
        intent.putExtra(TaskForm.KEY_TASK_UUID, binding.getTask().getUuid());
        startActivity(intent);
    }

    public void showContactEditView(Contact contact) {
        Intent intent = new Intent(getActivity(), ContactEditActivity.class);
        intent.putExtra(ContactEditActivity.KEY_CONTACT_UUID, contact.getUuid());
        intent.putExtra(TaskForm.KEY_TASK_UUID, binding.getTask().getUuid());
        startActivity(intent);
    }

    public void showEventEditView(Event event) {
        Intent intent = new Intent(getActivity(), EventEditActivity.class);
        intent.putExtra(EventEditActivity.KEY_EVENT_UUID, event.getUuid());
        intent.putExtra(TaskForm.KEY_TASK_UUID, binding.getTask().getUuid());
        startActivity(intent);
    }

    @Override
    public AbstractDomainObject getData() {
        return binding == null ? null : binding.getTask();
    }

}