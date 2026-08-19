// Sidebar.jsx
import { NavLink } from "react-router-dom";

const links = [
  { to: "/", label: "Dashboard" },
  { to: "/debt", label: "Debt" },
  { to: "/budget", label: "Budget" },
];

export default function Sidebar() {
  return (
    <nav className="w-56 border-r p-4">
      {links.map((l) => (
        <NavLink
          key={l.to}
          to={l.to}
          className={({ isActive }) =>
            `block px-3 py-2 rounded ${isActive ? "bg-gray-200 font-medium" : "hover:bg-gray-100"}`
          }
        >
          {l.label}
        </NavLink>
      ))}
    </nav>
  );
}