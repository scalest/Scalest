import React from "react"
import BoolInput from "./BoolInput"
import StringInput from "./StringInput"
import IntInput from "./IntInput"
import DoubleInput from "./DoubleInput"
import EnumInput from "./EnumInput"

const inputs = {
    "bool-input": BoolInput,
    "string-input": StringInput,
    "int-input": IntInput,
    "enum-input": EnumInput,
    "double-input": DoubleInput,
}

function DefaultInput() {
    return <span>No input for such type</span>
}

function FieldInput({ field, item, setItem }) {
    const SpecificInput = inputs[field.schema.inputType] || DefaultInput
    return <SpecificInput field={field} item={item} setItem={setItem} />
}

export default FieldInput