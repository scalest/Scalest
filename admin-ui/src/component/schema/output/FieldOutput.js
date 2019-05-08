import React from "react"
import BoolOutput from "./BoolOutput"
import StringOutput from "./StringOutput"
import IntOutput from "./IntOutput"
import DoubleOutput from "./DoubleOutput"
import EnumOutput from "./EnumOutput"

const outputs = {
    "bool-output": BoolOutput,
    "string-output": StringOutput,
    "int-output": IntOutput,
    "enum-output": EnumOutput,
    "double-output": DoubleOutput,
}

function DefaultOutput() {
    return <span>No output for such type</span>
}

function FieldOutput({ field, item, setItem }) {
    const SpecificOutput = outputs[field.schema.outputType] || DefaultOutput
    return <SpecificOutput field={field} item={item} setItem={setItem} />
}

export default FieldOutput