package com.intershop.platform.utils.pipelet.common;

import java.math.BigDecimal;

import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.core.capi.pipeline.PipelineInitializationException;

/**
 * The pipelet returns the result of a configurable operation on two operands.
 * The operation might be addition, subtraction, multiplication or division.
 * Since the operands are numbers the expected result type should be configured
 * too. The result type is used to attempt corresponding conversions to
 * primitive type on the operands. Any type of operands are supported including
 * operands with mixed types.
 */
public class CalculateDecimal extends Pipelet
{
    /**
     * Constant used to access the pipeline dictionary with key 'Op1Decimal'
     *
     * The first operand in case a BigDecimal is required by the operation.
     */
    public static final String DN_OP_1_DECIMAL = "Op1Decimal";

    /**
     * Constant used to access the pipeline dictionary with key 'Op1Double'
     *
     * The first operand in case a Double is required by the operation.
     */
    public static final String DN_OP_1_DOUBLE = "Op1Double";

    /**
     * Constant used to access the pipeline dictionary with key 'Op2Decimal'
     *
     * The second operand in case a BigDecimal is required by the operation.
     */
    public static final String DN_OP_2_DECIMAL = "Op2Decimal";

    /**
     * Constant used to access the pipeline dictionary with key 'Op2Int'
     *
     * The second operand in case an Integer is required by the operation.
     */
    public static final String DN_OP_2_INT = "Op2Int";
    
    /**
     * Constant used to access pipelet configuration with key 'Operation'
     */
    public static final String CN_OPERATION = "Operation";

    protected Operation cfgOperation;

    /**
     * Constant used to access the pipeline dictionary with key 'Result'
     *
     * The result of the calculation.
     */
    public static final String DN_RESULT = "Result";

    /**
     * The pipelet's execute method is called whenever the pipelets gets
     * executed in the context of a pipeline and a request. The pipeline
     * dictionary valid for the currently executing thread is provided as a
     * parameter.
     *
     * @param dict
     *            The pipeline dictionary to be used.
     * @throws PipeletExecutionException
     *             Thrown in case of severe errors that make the pipelet execute
     *             impossible (e.g. missing required input data).
     */
    public int execute(PipelineDictionary dict)
    {
        BigDecimal op1Decimal;
        if (cfgOperation.isOp1DecimalRequired())
        {
            op1Decimal = dict.getRequired(DN_OP_1_DECIMAL);
        }
        else
        {
            op1Decimal = dict.getOptional(DN_OP_1_DECIMAL);
        }

        Double op1Double;
        if (cfgOperation.isOp1DoubleRequired())
        {
            op1Double = dict.getRequired(DN_OP_1_DOUBLE);
        }
        else
        {
            op1Double = dict.getOptional(DN_OP_1_DOUBLE);
        }

        BigDecimal op2Decimal;
        if (cfgOperation.isOp2DecimalRequired())
        {
            op2Decimal = dict.getRequired(DN_OP_2_DECIMAL);
        }
        else
        {
            op2Decimal = dict.getOptional(DN_OP_2_DECIMAL);
        }

        Integer op2Int;
        if (cfgOperation.isOp2IntRequired())
        {
            op2Int = dict.getRequired(DN_OP_2_INT);
        }
        else
        {
            op2Int = dict.getOptional(DN_OP_2_INT);
        }

        BigDecimal result = cfgOperation.calculate(op1Decimal, op1Double, op2Decimal, op2Int);

        dict.put(DN_RESULT, result);

        return PIPELET_NEXT;
    }

    /**
     * The pipelet's initialization method is called whenever the pipeline used
     * to read and process pipelet configuration values that are required during
     * the pipelet execution later on.
     *
     * @throws PipelineInitializationException
     *             Thrown if some error occured when reading the pipelet
     *             configuration.
     */
    public void init() throws PipelineInitializationException
    {
        String cfg_operation = (String)getConfiguration().get(CN_OPERATION);
        if (null == cfg_operation)
        {
            throw new PipelineInitializationException(
                            "Mandatory attribute 'Operation' not found in pipelet configuration.");
        }
        this.cfgOperation = Operation.fromString(cfg_operation);
    }

    /**
     * A class that enumerates all supported operations.
     */
    protected enum Operation {
        ADD("+", true, false, true, false)
        {
            public BigDecimal calculate(BigDecimal op1Decimal, Double op1Double, BigDecimal op2Decimal, Integer op2Int)
            {
                return op1Decimal.add(op2Decimal);
            }
        },
        SUBTRACT("-", true, false, true, false)
        {
            public BigDecimal calculate(BigDecimal op1Decimal, Double op1Double, BigDecimal op2Decimal, Integer op2Int)
            {
                return op1Decimal.subtract(op2Decimal);
            }
        },
        MULTIPLY("*", true, false, true, false)
        {
            public BigDecimal calculate(BigDecimal op1Decimal, Double op1Double, BigDecimal op2Decimal, Integer op2Int)
            {
                return op1Decimal.multiply(op2Decimal);
            }
        },
        DIVIDE("/", true, false, true, false)
        {
            public BigDecimal calculate(BigDecimal op1Decimal, Double op1Double, BigDecimal op2Decimal, Integer op2Int)
            {
                return op1Decimal.divide(op2Decimal);
            }
        },
        NEGATE("Negate", true, false, false, false)
        {
            public BigDecimal calculate(BigDecimal op1Decimal, Double op1Double, BigDecimal op2Decimal, Integer op2Int)
            {
                return op1Decimal.negate();
            }
        },
        MIN("Min", true, false, true, false)
        {
            public BigDecimal calculate(BigDecimal op1Decimal, Double op1Double, BigDecimal op2Decimal, Integer op2Int)
            {
                return op1Decimal.min(op2Decimal);
            }
        },
        MAX("Max", true, false, true, false)
        {
            public BigDecimal calculate(BigDecimal op1Decimal, Double op1Double, BigDecimal op2Decimal, Integer op2Int)
            {
                return op1Decimal.max(op2Decimal);
            }
        },
        SCALE_BY_POWER_OF_TEN("ScaleByPowerOfTen", true, false, false, true)
        {
            public BigDecimal calculate(BigDecimal op1Decimal, Double op1Double, BigDecimal op2Decimal, Integer op2Int)
            {
                return op1Decimal.scaleByPowerOfTen(op2Int);
            }
        },
        CONVERT("Convert", false, true, false, false)
        {
            public BigDecimal calculate(BigDecimal op1Decimal, Double op1Double, BigDecimal op2Decimal, Integer op2Int)
            {
                return new BigDecimal(op1Double);
            }
        },
        ZERO("0", false, false, false, false)
        {
            public BigDecimal calculate(BigDecimal op1Decimal, Double op1Double, BigDecimal op2Decimal, Integer op2Int)
            {
                return BigDecimal.ZERO;
            }
        },
        ONE("1", false, false, false, false)
        {
            public BigDecimal calculate(BigDecimal op1Decimal, Double op1Double, BigDecimal op2Decimal, Integer op2Int)
            {
                return BigDecimal.ONE;
            }
        },
        TEN("10", false, false, false, false)
        {
            public BigDecimal calculate(BigDecimal op1Decimal, Double op1Double, BigDecimal op2Decimal, Integer op2Int)
            {
                return BigDecimal.TEN;
            }
        };

        private final String operationName;
        private boolean op1DecimalRequired;
        private boolean op1DoubleRequired;
        private boolean op2DecimalRequired;
        private boolean op2IntRequired;

        private Operation(String operationName, boolean op1DecimalRequired, boolean op1DoubleRequired, boolean op2DecimalRequired, boolean op2IntRequired)
        {
            this.operationName = operationName;
            this.op2DecimalRequired = op2DecimalRequired;
            this.op1DoubleRequired = op1DoubleRequired;
            this.op2DecimalRequired = op2DecimalRequired;
            this.op2IntRequired = op2IntRequired;
        }

        static Operation fromString(String operationName)
        {
            Operation[] allValues = values();
            for(Operation aValue : allValues)
            {
                if (aValue.operationName.equals(operationName))
                {
                    return aValue;
                }
            }

            throw new IllegalArgumentException("Unsupported operation: " + operationName);
        }
        
        public abstract BigDecimal calculate(BigDecimal op1Decimal, Double op1Double, BigDecimal op2Decimal, Integer op2Int);
        
        public boolean isOp1DecimalRequired()
        {
            return op1DecimalRequired;
        }
        
        public boolean isOp1DoubleRequired()
        {
            return op1DoubleRequired;
        }
        
        public boolean isOp2DecimalRequired()
        {
            return op2DecimalRequired;
        }
        
        public boolean isOp2IntRequired()
        {
            return op2IntRequired;
        }
    }
}
