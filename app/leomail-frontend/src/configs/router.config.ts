// src/router/index.ts
import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import MailView from "@/views/MailView.vue";
import Login from "@/views/Login.vue";
import PersonenView from "@/views/ContactView.vue";
import ProfilView from "@/views/ProfilView.vue";
import NewProject from "@/views/NewProject.vue";
import NewMail from "@/views/NewMail.vue";
import ProjectView from "@/views/ProjectView.vue";
import ScheduledMailsView from "@/views/ScheduledMailsView.vue";
import GroupView from "@/views/GroupView.vue";
import TemplateView from "@/views/TemplateView.vue";
import SettingsView from "@/views/SettingsView.vue";
import AuthorisationComponent from "@/views/AuthorisationView.vue";
import PostLoginView from "@/views/PostLoginView.vue";
import ImportingView from "@/views/ImportingView.vue"; // ImportingView importieren
import MailDetailView from "@/views/MailDetailView.vue";
import { useAuthStore } from "@/stores/auth.store";
import { refreshToken, validateToken } from "@/services/auth.service";
import { pinia } from "@/main";
import axiosInstance from "@/axiosInstance";
import { useImportStatusStore } from "@/stores/import-status.store";

const routes: Array<RouteRecordRaw> = [
  { path: '/login', name: 'login', component: Login },
  { path: '/importing', name: 'importing', component: ImportingView }, // Neue Route für ImportingView
  { path: '/post-login', name: 'post-login', component: PostLoginView, meta: { requiresAuth: true } },
  { path: '/mail', name: 'mail', component: MailView, meta: { requiresAuth: true } },
  { path: '/mail/new', name: 'newMail', component: NewMail, meta: { requiresAuth: true } },
  { path: '/scheduledMails', name: 'scheduled', component: ScheduledMailsView, meta: { requiresAuth: true } },
  { path: '/groups', name: 'groups', component: GroupView, meta: { requiresAuth: true } },
  { path: '/template', name: 'template', component: TemplateView, meta: { requiresAuth: true } },
  { path: '/settings', name: 'settings', component: SettingsView, meta: { requiresAuth: true } },
  { path: '/', name: 'projects', component: ProjectView, meta: { requiresAuth: true } },
  { path: '/projects/new', name: 'newProjects', component: NewProject, meta: { requiresAuth: true } },
  { path: '/contacts', name: 'contacts', component: PersonenView, meta: { requiresAuth: true } },
  { path: '/profile', name: 'profile', component: ProfilView, meta: { requiresAuth: true } },
  { path: '/authorisation', name: 'authorisation', component: AuthorisationComponent, meta: { requiresAuth: true } },
  {
    path: '/mail/:id/:projectId',
    name: 'MailDetail',
    component: MailDetailView,
    props: true,
    meta: { requiresAuth: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// Funktion zum Handhaben ungültiger Tokens
function handleInvalidToken(authStore, next) {
  authStore.logout();
  return next({ name: 'login' });
}

// Globale Navigation Guards
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore(pinia);
  const importStatusStore = useImportStatusStore();

  // Zuerst den Importstatus überprüfen
  try {
    await importStatusStore.updateImportStatus();
  } catch (error) {
    console.error('Fehler beim Aktualisieren des Importstatus:', error);
  }

  // Wenn der Import läuft und der Zielroute nicht ImportingView ist, umleiten
  if (importStatusStore.importing && to.name !== 'importing') {
    return next({ name: 'importing' });
  }

  // Prüfen, ob die Route Authentifizierung erfordert
  if (to.meta.requiresAuth) {
    const accessToken = authStore.accessToken;

    if (!accessToken) {
      return next({ name: 'login' });
    }

    try {
      const isValid = await validateToken();
      if (isValid) {
        axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;

        // Importstatus erneut aktualisieren
        await importStatusStore.updateImportStatus();

        if (importStatusStore.importing && to.name !== 'importing') {
          return next({ name: 'importing' });
        }

        return next();
      } else {
        try {
          const { access_token } = await refreshToken();
          authStore.setTokens(access_token, authStore.$state._refreshToken);
          axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${access_token}`;

          await importStatusStore.updateImportStatus();

          if (importStatusStore.importing && to.name !== 'importing') {
            return next({ name: 'importing' });
          }

          return next();
        } catch (refreshError) {
          return handleInvalidToken(authStore, next);
        }
      }
    } catch (error) {
      return handleInvalidToken(authStore, next);
    }
  } else {
    return next();
  }
});

export default router;