package controllers;

import dal.RepairOrderDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import models.RepairOrder;
import models.User;

public class EditOrderController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("Login");
            return;
        }

        String idRaw = request.getParameter("id");
        try {
            int orderId = Integer.parseInt(idRaw);
            RepairOrderDAO repairOrderDAO = new RepairOrderDAO();
            RepairOrder order = repairOrderDAO.GetOrderById(orderId);

            if (order == null) {
                response.sendRedirect("RepairOrder");
                return;
            }

            request.setAttribute("order", order);
            loadUsers(request, user);
            request.getRequestDispatcher("views/EditOrder.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect("RepairOrder");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("Login");
            return;
        }

        String error = "";
        int orderId = 0;
        String motoId = request.getParameter("motoId");
        String tech = request.getParameter("tech");
        String des = request.getParameter("des");
        String status = request.getParameter("status");

        try {
            orderId = Integer.parseInt(request.getParameter("orderId"));
        } catch (Exception e) {
            error = "Order ID must be a number.";
        }

        if (error.isEmpty()) {
            motoId = motoId.trim();
            tech = tech.trim();
            des = des.trim();
            status = status.trim();
            error = validateEditOrderFields(motoId, tech, des, status, orderId);
        }
    /** Extracted from doPost() to reduce Cognitive Complexity (was 25, limit 15). */
    private String validateEditOrderFields(String motoId, String tech, String des, String status, int orderId) {
        if (orderId <= 0) {
            return "Order ID must be greater than 0.";
        }
        if (motoId.isEmpty()) {
            return "Moto ID is required.";
        }
        if (!motoId.matches("^[0-9]{2}[A-Z0-9]{1,2}[0-9]{4,6}$")) {
            return "Invalid Moto ID format.";
        }
        if (tech.isEmpty()) {
            return "Technician is required.";
        }
        if (des.isEmpty()) {
            return "Description is required.";
        }
        if (des.length() > 255) {
            return "Description is too long.";
        }
        if (!List.of("PENDING", "PROCESSING", "COMPLETED").contains(status)) {
            return "Invalid status.";
        }
        return "";
    }

        if (!error.isEmpty()) {
            RepairOrder order = new RepairOrder();
            order.setOrderID(orderId);
            order.setMotoID(motoId == null ? "" : motoId);
            order.setTechnicianUsername(tech == null ? "" : tech);
            order.setDescription(des == null ? "" : des);
            order.setStatus(status == null ? "" : status);
            request.setAttribute("order", order);
            request.setAttribute("error", error);
            loadUsers(request, user);
            request.getRequestDispatcher("views/EditOrder.jsp").forward(request, response);
            return;
        }

        try {
            RepairOrderDAO repairOrderDAO = new RepairOrderDAO();
            RepairOrder order = new RepairOrder();
            order.setOrderID(orderId);
            order.setMotoID(motoId);
            order.setTechnicianUsername(tech);
            order.setDescription(des);
            repairOrderDAO.UpdateOrder(order);
            repairOrderDAO.UpdateStatus(orderId, status);
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Unable to update the repair order.");
        }

        response.sendRedirect("RepairOrder");
    }

    private void loadUsers(HttpServletRequest request, User user) {
        UserDAO userDAO = new UserDAO();
        List<User> users = userDAO.GetUsers();
        if (user.getRole().getRoleName().equalsIgnoreCase("staff")) {
            List<User> techList = new ArrayList<>();
            for (User u : users) {
                if ("staff".equalsIgnoreCase(u.getRole().getRoleName())) {
                    techList.add(u);
                }
            }
            request.setAttribute("users", techList);
        } else {
            request.setAttribute("users", users);
        }
    }
}
