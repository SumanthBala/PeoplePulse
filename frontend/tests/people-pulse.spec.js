import { test, expect } from '@playwright/test';
const unique = prefix => `${prefix}-${Date.now()}`;
async function login(page, username, password){
  await page.goto('/');
  await page.getByLabel('Username').fill(username);
  await page.getByLabel('Password').fill(password);
  await page.getByRole('button', {name:'Sign in'}).click();
}

test.describe('People Pulse UI', () => {
  test('admin login and existing employees load from H2', async ({ page }) => {
    await login(page,'admin','admin123');
    await expect(page.getByRole('heading',{name:'Dashboard'})).toBeVisible();
    await page.getByRole('button',{name:'Employees'}).click();
    await expect(page.getByRole('heading',{name:'Employee directory'})).toBeVisible();
    await expect(page.locator('tbody tr')).not.toHaveCount(0);
  });

  test('admin creates a project and it survives reload', async ({ page }) => {
    const name=unique('UI Project');
    await login(page,'admin','admin123');
    await page.getByRole('button',{name:'Projects'}).click();
    await page.getByLabel('Project name').fill(name);
    await page.getByLabel('Budget').fill('250000');
    await page.getByRole('button',{name:'Create Project'}).click();
    await expect(page.getByText(name,{exact:true})).toBeVisible();
    await page.reload();
    await page.getByRole('button',{name:'Projects'}).click();
    await expect(page.getByText(name,{exact:true})).toBeVisible();
  });

  test('admin creates an employee against a project', async ({ page }) => {
    const project=unique('Employee Test Project');
    await login(page,'admin','admin123');
    await page.getByRole('button',{name:'Projects'}).click();
    await page.getByLabel('Project name').fill(project);
    await page.getByLabel('Budget').fill('500000');
    await page.getByRole('button',{name:'Create Project'}).click();
    await page.getByRole('button',{name:'Employees'}).click();
    const name=unique('UI Employee');
    await page.getByLabel('Name').fill(name);
    await page.getByLabel('Email').fill(`${Date.now()}@example.com`);
    await page.getByLabel('City').fill('Hyderabad');
    await page.getByLabel('Annual salary').fill('60000');
    await page.getByLabel('Project').selectOption({label:new RegExp(project)});
    await page.getByRole('button',{name:'Add Employee'}).click();
    await expect(page.getByText(name,{exact:true})).toBeVisible();
  });

  test('admin sees an error when employee allocation exceeds project budget', async ({ page }) => {
    const project=unique('Budget Test Project');
    await login(page,'admin','admin123');
    await page.getByRole('button',{name:'Projects'}).click();
    await page.getByLabel('Project name').fill(project);
    await page.getByLabel('Budget').fill('1000');
    await page.getByRole('button',{name:'Create Project'}).click();
    await page.getByRole('button',{name:'Employees'}).click();
    await page.getByLabel('Name').fill(unique('Over Budget Employee'));
    await page.getByLabel('Email').fill(`${Date.now()}@example.com`);
    await page.getByLabel('City').fill('Hyderabad');
    await page.getByLabel('Annual salary').fill('5000');
    await page.getByLabel('Project').selectOption({label:new RegExp(project)});
    await page.getByRole('button',{name:'Add Employee'}).click();
    await expect(page.getByRole('alert')).toContainText('Out of budget');
  });

  test('candidate can log in and submit an application', async ({ page }) => {
    await login(page,'candidate','candidate123');
    await expect(page.getByRole('heading',{name:'My Applications'})).toBeVisible();
    await page.getByLabel('Phone').fill('9876543210');
    await page.getByLabel('Skills').fill('Java, Spring Boot, React');
    await page.getByLabel('Applied role').fill('Senior Java Developer');
    await page.getByRole('button',{name:'Submit Application'}).click();
    await expect(page.getByText('Application submitted successfully.')).toBeVisible();
    await expect(page.getByText('PENDING',{exact:true})).toBeVisible();
  });

  test('manager can view applications and shortlist a candidate', async ({ page }) => {
    await login(page,'manager','manager123');
    await expect(page.getByRole('heading',{name:'Recruitment'})).toBeVisible();
    const row=page.locator('tbody tr').filter({hasText:'candidate@example.com'}).first();
    await expect(row).toBeVisible();
    await row.getByRole('button',{name:'Shortlist'}).click();
    await expect(row.getByText('SHORTLISTED',{exact:true})).toBeVisible();
  });

  test('candidate sees shortlist status after manager action', async ({ page }) => {
    await login(page,'candidate','candidate123');
    await expect(page.getByText('SHORTLISTED',{exact:true})).toBeVisible();
  });

  test('invalid login is rejected', async ({ page }) => {
    await page.goto('/');
    await page.getByLabel('Username').fill('admin');
    await page.getByLabel('Password').fill('wrong');
    await page.getByRole('button',{name:'Sign in'}).click();
    await expect(page.getByRole('alert')).toContainText('Invalid username or password');
  });
});
