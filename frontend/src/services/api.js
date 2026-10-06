const API_URL = (import.meta.env.VITE_API_URL || '').replace(/\/$/, '');

const apiUrl = (url) => `${API_URL}${url}`;

const request = async (url, options = {}) => {
  const response = await fetch(apiUrl(url), {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    },
    ...options
  });
  const data = await response.json().catch(() => null);
  if (!response.ok) throw new Error(data?.message || `Request failed (${response.status})`);
  return data;
};

export const login = (username,password)=>request('/api/auth/login',{method:'POST',body:JSON.stringify({username,password})});
export const dbLogin = (username,password)=>request('/api/auth/db-login',{method:'POST',body:JSON.stringify({username,password})});
export const getEmployees=()=>request('/api/employees');
export const saveEmployee=e=>request('/api/employees',{method:'POST',body:JSON.stringify(e)});
export const deleteEmployee=id=>request(`/api/employees/${id}`,{method:'DELETE'});
export const getProjects=()=>request('/api/projects');
export const saveProject=p=>request('/api/projects',{method:'POST',body:JSON.stringify(p)});
export const updateProject=(id,p)=>request(`/api/projects/${id}`,{method:'PUT',body:JSON.stringify(p)});
export const getApplications=()=>request('/api/applications');
export const getCandidateApplications=email=>request(`/api/applications/candidate/${encodeURIComponent(email)}`);
export const getCandidateApplicationsByUsername=username=>request(`/api/applications/candidate/username/${encodeURIComponent(username)}`);
export const applyCandidate=a=>request('/api/applications',{method:'POST',body:JSON.stringify(a)});
export const managerDecision=(id,status,comment='')=>request(`/api/applications/${id}/manager`,{method:'PUT',body:JSON.stringify({status,comment})});
export const adminDecision=(id,status,comment='')=>request(`/api/applications/${id}/admin`,{method:'PUT',body:JSON.stringify({status,comment})});
export const managerL1Complete=id=>request(`/api/applications/${id}/manager/l1-complete`,{method:'PUT'});
export const adminFinalApprove=id=>request(`/api/applications/${id}/admin/final-approve`,{method:'PUT'});

export const managerComment=(id,comment='')=>request(`/api/applications/${id}/manager/comment`,{method:'PUT',body:JSON.stringify({status:'MANAGER_COMMENT',comment})});

export const uploadResume=(id,file)=>{const fd=new FormData();fd.append("resume",file);return fetch(apiUrl(`/api/applications/${id}/resume`),{method:"POST",body:fd}).then(async r=>{const d=await r.json().catch(()=>null);if(!r.ok)throw new Error(d?.message||`Request failed (${r.status})`);return d;});};
export const chatWithPeoplePulseAI=(payload)=>request('/api/ai/chat',{method:'POST',body:JSON.stringify(payload)});
export const getEmployeeRequests=()=>request('/api/employee-requests');
export const getManagerEmployeeRequests=username=>request(`/api/employee-requests/manager/${encodeURIComponent(username)}`);
export const submitEmployeeRequest=r=>request('/api/employee-requests',{method:'POST',body:JSON.stringify(r)});
export const approveEmployeeRequest=id=>request(`/api/employee-requests/${id}/approve`,{method:'PUT'});
export const rejectEmployeeRequest=(id,comment)=>request(`/api/employee-requests/${id}/reject`,{method:'PUT',body:JSON.stringify({comment})});

export const getDatabaseStatus=()=>request('/api/db/status');

export const getDatabaseTables=()=>request('/api/db/tables');
export const getLoginActivity=()=>request('/api/db/login-activity');
export const getDatabaseQuery=(sql)=>request('/api/db/query',{method:'POST',body:JSON.stringify({sql})});
export const getDatabaseAnalytics=()=>request('/api/db/analytics');
export const getUnlockRequests=()=>request('/api/db/unlock-requests');
export const approveUnlock=username=>request(`/api/db/unlock-requests/${encodeURIComponent(username)}/approve`,{method:'PUT'});
export const changePassword=(username,currentPassword,newPassword)=>request('/api/auth/change-password',{method:'POST',body:JSON.stringify({username,currentPassword,newPassword})});

export const getProfile=(username)=>request(`/api/profile/${encodeURIComponent(username)}`);
export const updateProfile=(username,profile)=>request(`/api/profile/${encodeURIComponent(username)}`,{method:'PUT',body:JSON.stringify(profile)});
export const uploadProfilePhoto=(username,file)=>{const fd=new FormData();fd.append('photo',file);return fetch(apiUrl(`/api/profile/${encodeURIComponent(username)}/photo`),{method:'POST',body:fd}).then(async r=>{const d=await r.json().catch(()=>null);if(!r.ok)throw new Error(d?.message||`Request failed (${r.status})`);return d;});};
export const removeProfilePhoto=(username)=>request(`/api/profile/${encodeURIComponent(username)}/photo`,{method:'DELETE'});
