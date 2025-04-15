package org.training;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Mealy Machine Simulator using Java Swing.
 *
 * The program simulates a Mealy Machine based on user input. The user will provide:
 *   - The set of states (the first state is always the start state)
 *   - The input alphabet (symbols used to form the input string)
 *   - The output alphabet
 *   - A transition diagram: each line contains a state followed by transition definitions for each symbol in the input alphabet
 *     Each transition is given in the format "nextState/output", and the items are separated by TAB characters
 *   - An input string
 *
 * The simulation calculates the transitions for the given input string, displays the sequence of states and produced outputs,
 * and shows the simulation in two graphics:
 *    1) A diagram with state nodes and arrows indicating transitions (with input/output labels)
 *    2) A detailed transition table displayed in a JTable
 */
public class MealeySimulatorGUI extends JFrame {

    // UI components
    private JTextField txtStates;
    private JTextField txtInputAlphabet;
    private JTextField txtOutputAlphabet;
    private JTextArea areaTransitionDiagram;
    private JTextField txtInputString;
    private JTextArea areaResult;
    private SimulationPanel simulationPanel;
    private JTable tableDetails;
    private DefaultTableModel tableModel;

    public MealeySimulatorGUI() {
        setTitle("Mealy Machine Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel for user input
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Row 1: States
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("States (comma separated, first state is start state):"), gbc);
        txtStates = new JTextField("q0,q1,q2,q3");
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        inputPanel.add(txtStates, gbc);

        // Row 2: Input Alphabet
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        inputPanel.add(new JLabel("Input Alphabet (comma separated):"), gbc);
        txtInputAlphabet = new JTextField("a,b");
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        inputPanel.add(txtInputAlphabet, gbc);

        // Row 3: Output Alphabet
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        inputPanel.add(new JLabel("Output Alphabet (comma separated):"), gbc);
        txtOutputAlphabet = new JTextField("0,1");
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        inputPanel.add(txtOutputAlphabet, gbc);

        // Row 4: Transition Diagram
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        areaTransitionDiagram = new JTextArea(5, 40);
        areaTransitionDiagram.setBorder(BorderFactory.createTitledBorder("Transition Diagram (each line: <state> TAB <symbol1: nextState/output> TAB <symbol2: nextState/output> ...):"));
        // Sample data:
        areaTransitionDiagram.setText("q0\tq1/0\tq0/1\nq1\tq2/0\tq0/0\nq2\tq2/0\tq3/1\nq3\tq1/0\tq0/0");
        inputPanel.add(new JScrollPane(areaTransitionDiagram), gbc);

        // Row 5: Input String
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Input String:"), gbc);
        txtInputString = new JTextField("abab");
        gbc.gridx = 1;
        inputPanel.add(txtInputString, gbc);

        // Row 6: Simulate Button
        JButton btnSimulate = new JButton("Simulate");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(btnSimulate, gbc);

        add(inputPanel, BorderLayout.NORTH);

        // Output Panel: Contains result text, simulation diagram and detailed table
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.Y_AXIS));

        // Result Text Area
        areaResult = new JTextArea(5, 40);
        areaResult.setEditable(false);
        areaResult.setBorder(BorderFactory.createTitledBorder("Simulation Result"));
        outputPanel.add(new JScrollPane(areaResult));

        // Simulation Diagram Panel
        simulationPanel = new SimulationPanel();
        simulationPanel.setPreferredSize(new Dimension(600, 300));
        simulationPanel.setBackground(Color.WHITE);
        JPanel simPanelContainer = new JPanel(new BorderLayout());
        simPanelContainer.setBorder(new TitledBorder("State Transition Diagram"));
        simPanelContainer.add(simulationPanel, BorderLayout.CENTER);
        outputPanel.add(simPanelContainer);

        // Detailed Transition Table Panel
        String[] columnNames = {"Step", "Input", "Old State", "New State", "Output"};
        tableModel = new DefaultTableModel(columnNames, 0);
        tableDetails = new JTable(tableModel);
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new TitledBorder("Detailed Transition Table"));
        tablePanel.add(new JScrollPane(tableDetails), BorderLayout.CENTER);
        tablePanel.setPreferredSize(new Dimension(600, 150));
        outputPanel.add(tablePanel);

        add(outputPanel, BorderLayout.CENTER);

        // Button event: Run simulation on button click
        btnSimulate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runSimulation();
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Runs the Mealy machine simulation based on user input
     * The transition diagram is read as a mapping:
     *  Map<state, Map<inputSymbol, Transition>>
     * Transition holds next state and output
     */
    private void runSimulation() {
        try {
            // 1. Read input data
            String[] states = txtStates.getText().trim().split("\\s*,\\s*");
            if (states.length == 0) {
                throw new Exception("State information is empty!");
            }
            String[] inputAlphabet = txtInputAlphabet.getText().trim().split("\\s*,\\s*");
            String[] outputAlphabet = txtOutputAlphabet.getText().trim().split("\\s*,\\s*");

            // 2. Parse transition diagram
            // Structure: Map<state, Map<inputSymbol, Transition>>
            Map<String, Map<String, Transition>> transitionDiagram = new HashMap<>();
            String[] lines = areaTransitionDiagram.getText().trim().split("\\n");
            for (String line : lines) {
                String[] tokens = line.split("\\t");
                if (tokens.length < inputAlphabet.length + 1) {
                    throw new Exception("Not enough columns in transition diagram: " + line);
                }
                String currentState = tokens[0].trim();
                Map<String, Transition> rowMap = new HashMap<>();
                for (int i = 0; i < inputAlphabet.length; i++) {
                    String symbol = inputAlphabet[i].trim();
                    String token = tokens[i + 1].trim();
                    if (!token.contains("/")) {
                        throw new Exception("Transition token '" + token + "' must be in the format nextState/output!");
                    }
                    String[] parts = token.split("/", 2);
                    String nextState = parts[0].trim();
                    String outputSymbol = parts[1].trim();
                    rowMap.put(symbol, new Transition(nextState, outputSymbol));
                }
                transitionDiagram.put(currentState, rowMap);
            }

            // 3. Read input string
            String inputString = txtInputString.getText().trim();
            if (inputString.isEmpty()) {
                throw new Exception("Input string is empty!");
            }

            // 4. Perform simulation: start from the first state (states[0])
            List<String> simulationPath = new ArrayList<>();
            List<String> producedOutputs = new ArrayList<>();
            List<String> inputForTransitions = new ArrayList<>();
            // For detailed table: record each transition step
            List<TransitionRecord> transitionRecords = new ArrayList<>();

            String currentState = states[0];
            simulationPath.add(currentState);
            // In Mealy machines, the start state's output is not produced before a transition.

            for (int i = 0; i < inputString.length(); i++) {
                String symbol = String.valueOf(inputString.charAt(i));
                if (!transitionDiagram.containsKey(currentState)) {
                    throw new Exception("State not found in transition diagram: " + currentState);
                }
                Map<String, Transition> transitions = transitionDiagram.get(currentState);
                if (!transitions.containsKey(symbol)) {
                    throw new Exception("Transition for (" + currentState + ", " + symbol + ") is not defined!");
                }
                Transition t = transitions.get(symbol);
                String oldState = currentState;
                currentState = t.nextState;
                simulationPath.add(currentState);
                producedOutputs.add(t.output);
                inputForTransitions.add(symbol);
                transitionRecords.add(new TransitionRecord(i+1, symbol, oldState, currentState, t.output));
            }

            // 5. Display textual results
            StringBuilder sb = new StringBuilder();
            sb.append("State Transitions: ");
            for (int i = 0; i < simulationPath.size(); i++) {
                sb.append(simulationPath.get(i));
                if (i < simulationPath.size() - 1) {
                    sb.append(" -> ");
                }
            }
            sb.append("\nProduced Output: ");
            for (String out : producedOutputs) {
                sb.append(out);
            }
            areaResult.setText(sb.toString());

            // 6. Update the simulation diagram panel
            simulationPanel.setSimulationData(simulationPath, inputForTransitions, producedOutputs);
            simulationPanel.repaint();

            // 7. Update the detailed transition table
            updateTransitionTable(transitionRecords);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Helper method to update the JTable for detailed transitions
    private void updateTransitionTable(List<TransitionRecord> records) {
        tableModel.setRowCount(0); // clear previous data
        for (TransitionRecord r : records) {
            Object[] row = { r.step, r.inputSymbol, r.oldState, r.newState, r.output };
            tableModel.addRow(row);
        }
    }

    // Helper class to store each transition record
    private static class TransitionRecord {
        int step;
        String inputSymbol;
        String oldState;
        String newState;
        String output;
        public TransitionRecord(int step, String inputSymbol, String oldState, String newState, String output) {
            this.step = step;
            this.inputSymbol = inputSymbol;
            this.oldState = oldState;
            this.newState = newState;
            this.output = output;
        }
    }

    // Helper class to store transition information for each symbol
    private static class Transition {
        String nextState;
        String output;
        public Transition(String nextState, String output) {
            this.nextState = nextState;
            this.output = output;
        }
    }

    /**
     * SimulationPanel: Draws the state transition diagram.
     * States are drawn as circles and transitions as arrows with input/output labels.
     */
    class SimulationPanel extends JPanel {
        private List<String> simPath = new ArrayList<>();
        private List<String> inputs = new ArrayList<>();
        private List<String> outputs = new ArrayList<>();

        public void setSimulationData(List<String> path, List<String> inputs, List<String> outputs) {
            this.simPath = path;
            this.inputs = inputs;
            this.outputs = outputs;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (simPath == null || simPath.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int n = simPath.size();
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int margin = 50;
            int r = 20;  // radius for state circles
            int gap = (n > 1) ? (panelWidth - 2 * margin) / (n - 1) : 0;

            // Calculate center point for each state circle.
            List<Point> centers = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                int x = margin + i * gap;
                int y = panelHeight / 2;
                centers.add(new Point(x, y));
            }

            // Draw state circles
            for (int i = 0; i < n; i++) {
                Point p = centers.get(i);
                g2.setColor(Color.LIGHT_GRAY);
                g2.fillOval(p.x - r, p.y - r, 2 * r, 2 * r);
                g2.setColor(Color.BLACK);
                g2.drawOval(p.x - r, p.y - r, 2 * r, 2 * r);
                String stateName = simPath.get(i);
                FontMetrics fm = g2.getFontMetrics();
                int strWidth = fm.stringWidth(stateName);
                int strHeight = fm.getAscent();
                g2.drawString(stateName, p.x - strWidth / 2, p.y + strHeight / 2);
            }

            // Draw arrows between states with input/output labels
            for (int i = 0; i < n - 1; i++) {
                Point p1 = centers.get(i);
                Point p2 = centers.get(i+1);
                double angle = Math.atan2(p2.y - p1.y, p2.x - p1.x);
                int x1 = (int)(p1.x + r * Math.cos(angle));
                int y1 = (int)(p1.y + r * Math.sin(angle));
                int x2 = (int)(p2.x - r * Math.cos(angle));
                int y2 = (int)(p2.y - r * Math.sin(angle));
                g2.drawLine(x1, y1, x2, y2);

                // Draw arrow head
                int arrowSize = 6;
                Polygon arrowHead = new Polygon();
                arrowHead.addPoint(0, 0);
                arrowHead.addPoint(-arrowSize, -arrowSize);
                arrowHead.addPoint(arrowSize, -arrowSize);
                g2.translate(x2, y2);
                g2.rotate(angle);
                g2.fill(arrowHead);
                g2.rotate(-angle);
                g2.translate(-x2, -y2);

                // Draw the label (input / output) on the arrow
                String inputSymbol = (i < inputs.size()) ? inputs.get(i) : "";
                String outSymbol = (i < outputs.size()) ? outputs.get(i) : "";
                String label = inputSymbol + " / " + outSymbol;
                int labelX = (x1 + x2) / 2;
                int labelY = (y1 + y2) / 2 - 10;
                g2.drawString(label, labelX, labelY);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MealeySimulatorGUI().setVisible(true));
    }
}
