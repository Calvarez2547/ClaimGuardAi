import {
  BarChart3,
  Bell,
  ClipboardList,
  FileSearch,
  Gauge,
  HelpCircle,
  LogOut,
  Menu,
  PlusCircle,
  Search,
  Settings,
  ShieldCheck,
  Sparkles,
  Users,
} from 'lucide-react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { initials } from '../utils/format';

const navItems = [
  { to: '/', label: 'Dashboard', icon: Gauge },
  { to: '/claims', label: 'Claims', icon: ClipboardList },
  { to: '/claims/new', label: 'Create Claim', icon: PlusCircle },
  { to: '/analysis-history', label: 'Analyses', icon: FileSearch },
  { to: '/reports', label: 'Reports', icon: BarChart3 },
  { to: '/settings', label: 'Settings', icon: Settings },
];

function pageTitle(pathname: string) {
  if (pathname === '/') return ['Dashboard', 'Overview of claim review and AI analysis performance'];
  if (pathname.startsWith('/claims/new')) return ['Create Claim', 'Add a fake/demo claim for local review'];
  if (pathname.startsWith('/claims/')) return ['Claim Detail / AI Analysis', 'Review claim data, notes, and backend-owned analysis'];
  if (pathname.startsWith('/claims')) return ['Claims', 'Review, analyze, and manage your claims'];
  if (pathname.startsWith('/analysis-history')) return ['Analysis History', 'Recent persisted AI-assisted claim analyses'];
  if (pathname.startsWith('/reports')) return ['Reports', 'Prototype reporting workspace'];
  if (pathname.startsWith('/admin/users')) return ['Admin — Users', 'Manage user accounts and roles'];
  if (pathname.startsWith('/audit')) return ['Audit Log', 'Security and activity event history'];
  return ['Settings', 'Prototype configuration workspace'];
}

export function AppLayout() {
  const { user, logout, hasRole } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [title, subtitle] = pageTitle(location.pathname);

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <div className="grid grid-cols-[260px_minmax(0,1fr)] max-lg2:grid-cols-[88px_minmax(0,1fr)] max-md2:grid-cols-[1fr] min-h-screen">
      {/* Sidebar */}
      <aside className="sticky top-0 max-md2:static flex flex-col gap-6 h-screen max-md2:h-auto p-[26px_20px] text-navy-text bg-gradient-to-b from-navy to-navy-dark overflow-y-auto">
        {/* Brand */}
        <div className="flex items-center gap-3 text-[22px] font-extrabold">
          <div className="grid w-10 h-10 place-items-center border border-navy-accent/40 rounded bg-[rgba(25,119,210,0.16)] text-navy-accent">
            <ShieldCheck size={27} />
          </div>
          <span className="max-lg2:text-[0] max-md2:text-[22px]">
            ClaimGuard <strong className="text-navy-accent">AI</strong>
          </span>
        </div>

        {/* Nav */}
        <nav className="grid gap-2 max-md2:grid-cols-2" aria-label="Primary navigation">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) =>
                `flex items-center gap-3 min-h-[46px] px-3 rounded font-bold text-navy-text
                 max-lg2:justify-center max-lg2:px-0 max-md2:justify-start max-md2:px-3
                 ${isActive ? 'text-white bg-gradient-to-br from-primary to-primary-hover' : 'hover:text-white hover:bg-gradient-to-br hover:from-primary hover:to-primary-hover'}`
              }
            >
              <item.icon size={19} />
              <span className="max-lg2:text-[0] max-md2:text-base">{item.label}</span>
              {item.label === 'Analyses' ? (
                <span className="ml-auto max-lg2:hidden px-2 py-0.5 rounded-full text-[#cde9ff] bg-primary/35 text-xs">AI</span>
              ) : null}
            </NavLink>
          ))}
        </nav>

        {/* Admin links — ADMINISTRATOR only */}
        {hasRole('ADMINISTRATOR') && (
          <div className="grid gap-1">
            <p className="m-0 text-[11px] font-extrabold uppercase tracking-widest text-navy-text/60 px-3">Admin</p>
            {[
              { to: '/admin/users', label: 'Users', icon: Users },
              { to: '/audit', label: 'Audit Log', icon: ShieldCheck },
            ].map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `flex items-center gap-3 min-h-[40px] px-3 rounded font-bold text-navy-text
                   max-lg2:justify-center max-lg2:px-0 max-md2:justify-start max-md2:px-3
                   ${isActive ? 'text-white bg-gradient-to-br from-primary to-primary-hover' : 'hover:text-white hover:bg-gradient-to-br hover:from-primary hover:to-primary-hover'}`
                }
              >
                <item.icon size={17} />
                <span className="max-lg2:text-[0] max-md2:text-base">{item.label}</span>
              </NavLink>
            ))}
          </div>
        )}

        {/* AI card */}
        <div className="grid gap-2 mt-auto p-4 border border-white/15 rounded bg-white/[0.06] text-[#eff8ff] max-lg2:hidden">
          <Sparkles size={18} />
          <strong>AI Review Assist</strong>
          <p className="text-[#a9c7e8] text-[13px] m-0">Runs through the Spring Boot backend only.</p>
        </div>

        {/* Footer */}
        <div className="flex gap-2.5 text-[#a9c7e8] text-[13px] max-lg2:hidden">
          <HelpCircle size={18} />
          <span>Prototype support</span>
        </div>
      </aside>

      {/* Main */}
      <main className="min-w-0">
        {/* Topbar */}
        <header className="sticky top-0 z-10 flex items-center gap-5 min-h-[82px] px-8 py-4 max-md2:flex-col max-md2:items-start max-md2:px-[18px] border-b border-app-border bg-white/94 backdrop-blur-[10px]">
          <div className="hidden max-md2:block">
            <Menu size={22} />
          </div>
          <div className="min-w-[220px] mr-auto">
            <h1 className="m-0 text-app-text text-2xl leading-tight">{title}</h1>
            <p className="mt-1 mb-0 text-app-muted text-sm">{subtitle}</p>
          </div>
          <div className="flex items-center gap-3 max-md2:w-full max-md2:flex-wrap">
            <label className="flex items-center gap-2.5 min-w-[320px] max-md2:min-w-0 max-md2:w-full px-3 border border-app-border rounded text-app-muted bg-white">
              <Search size={17} />
              <input
                className="w-full h-[42px] border-0 outline-0 text-app-text bg-transparent"
                placeholder="Search claims, IDs, providers..."
                aria-label="Search"
              />
            </label>
            <button className="relative grid w-[42px] h-[42px] place-items-center border border-app-border rounded text-app-text bg-white" type="button" title="Notifications">
              <Bell size={18} />
              <span className="absolute -top-[5px] -right-[5px] grid w-[18px] h-[18px] place-items-center rounded-full bg-c-red text-white text-[11px]">3</span>
            </button>
            <div className="flex items-center gap-2.5">
              <span className="grid w-[38px] h-[38px] place-items-center rounded-full bg-[#17345c] text-white text-[13px] font-extrabold">
                {initials(user?.username)}
              </span>
              <div>
                <strong className="block text-sm">{user?.username || 'Analyst'}</strong>
                <p className="m-0 text-app-muted text-xs">{user?.roles?.[0]?.replace(/_/g, ' ') || 'USER'}</p>
              </div>
            </div>
            <button
              className="inline-flex items-center justify-center gap-2 min-h-[40px] px-[14px] rounded font-extrabold border border-app-border text-app-text bg-white"
              type="button"
              onClick={handleLogout}
            >
              <LogOut size={17} />
              Log out
            </button>
          </div>
        </header>

        {/* Content */}
        <section className="p-[28px_32px_40px] max-md2:p-[18px]">
          <Outlet />
        </section>
      </main>
    </div>
  );
}
